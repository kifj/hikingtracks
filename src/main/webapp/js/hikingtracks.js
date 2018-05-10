var client = new Client();
var baseurl = "../services";
var serviceUrl = "/hikingtracks/1.0/";
var serviceTrackUrl = serviceUrl + "tracks/";
var serviceUserUrl = serviceUrl + "user/";
var serviceSearchUrl = serviceUrl + "search";

function Client() {
  this.trackData = null;
  this.profileData = {};
  this.token = $.cookie('x-auth-token');
  this.showBanner = ($.cookie('show-banner') == 'false') ? false : true;
  this.maxResults = 5;
  this.maxPagers = 7;
}

// ---------------------------------------------------------------------

Client.prototype.initTrackPage = function() {
  var caller = this;
  if (!this.token) {
    // prevent showing page from cache
    var name = caller.nameFromAnchor();
    window.location.href = "../login.html?from=" + encodeURIComponent("secure/track.html#" + name);
    return;
  }
  var language = $.cookie('language');
  if (language == "en") {
    language = "";
  }
  $.datepicker.setDefaults($.datepicker.regional[language]);
  $("#track_date").datepicker({
    changeMonth : true,
    changeYear : true
  }).datepicker("option", "showAnim", "slideDown");
  $("#button_add_track_data").click(function(e) {
    caller.addTrackData();
  });
  $("#button_add_image").click(function(e) {
    caller.addImage();
  });
  $("#confirmation-dialog").dialog({
    autoOpen : false,
    height : 175,
    width : 250,
    modal : true,
    closeOnEscape : true,
    resizable : false,
    buttons : {
      "Delete" : function() {
        $(this).dialog("close");
        caller.makeTrackDelete();
      },
      "Cancel" : function() {
        $(this).dialog("close");
      }
    },
  });
  $("#button_delete").click(function(e) {
    $("#confirmation-message").text(msg_confirm_delete);
    $("#confirmation-dialog").dialog("open");
  });
  $("#button_refresh").click(function(e) {
    caller.loadTrack(caller.trackData.name);
  });
  $("#button_show").click(function(e) {
    caller.showTrackDetails(caller.trackData);
  });
  $("#button_list").click(function(e) {
    caller.showTrackList();
  });
  $('#kmlupload').fileupload({
    dataType : 'json',
    url : baseurl + serviceTrackUrl,
    type : 'POST',
    multipart : false,
    sequentialUploads: true
  });
  $('#kmlupload').bind('fileuploadadd', function(e, data) {
    var trackData = caller.trackData;
    data.url = baseurl + serviceTrackUrl + trackData.name + '/kml/';
  });
  $('#kmlupload').bind('fileuploadstart', function(e) {
    caller.statusOn(msg_uploading);
    caller.progressOn();
  });
  $('#kmlupload').bind('fileuploadprogressall', function(e, data) {
    var progress = parseInt(data.loaded / data.total * 100, 10);
    caller.statusOn(msg_uploading_percent(progress));
    $("#progressbar").progressbar({
      value : progress
    });
  });
  $('#kmlupload').bind('fileuploadstop', function(e) {
    var trackData = caller.trackData;
    caller.loadTrack(trackData.name);
    caller.statusOff();
    caller.progressOff();
  });
  $('#imgupload').fileupload({
    dataType : 'json',
    url : baseurl + serviceTrackUrl,
    type : 'POST',
    multipart : false,
    sequentialUploads: true
  });
  $('#imgupload').bind('fileuploadadd', function(e, data) {
    var trackData = caller.trackData;
    data.url = baseurl + serviceTrackUrl + trackData.name + '/images/';
  });
  $('#imgupload').bind('fileuploadstart', function(e) {
    caller.statusOn(msg_uploading);
    caller.progressOn();
  });
  $('#imgupload').bind('fileuploadprogressall', function(e, data) {
    var progress = parseInt(data.loaded / data.total * 100, 10);
    caller.statusOn(msg_uploading_percent(progress));
    $("#progressbar").progressbar({
      value : progress
    });
  });
  $('#imgupload').bind('fileuploadstop', function(e) {
    var trackData = caller.trackData;
    caller.loadTrack(trackData.name);
    caller.statusOff();
    caller.progressOff();
  });
  $(".fg-button:not(.ui-state-disabled)").hover(function() {
    $(this).addClass("ui-state-hover");
  }, function() {
    $(this).removeClass("ui-state-hover");
  }).mousedown(function() {
    $(this).addClass("ui-state-active");
  }).mouseup(function() {
    $(this).removeClass("ui-state-active");
  });
  this.showLoginButtons();
  this.loadTrack();
}

Client.prototype.initDetailPage = function() {
  var caller = this;
  if (this.token) {
    $("#button_edit").click(function(e) {
      caller.editTrack(caller.trackData);
    });
  } else {
    $("#button_edit").remove();
    $("#_public").css("display", "none");
  }
  $("#button_list").click(function(e) {
    caller.showTrackList();
  });
  $("#search_query").autocomplete({
    source : function(request, response) {
      caller.autocomplete(request, response, (this.token != null));
    },
    minLength : 2,
    select : function(event, ui) {
      if (ui.item && ui.item.value) {
        caller.loadOverview(ui.item.value);
        this.value = '';
      }
    },
    open : function() {
      $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
    },
    close : function() {
      $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
    }
  });
  this.showLoginButtons();
  this.loadTrackDetail(null, false, false, true);
}

Client.prototype.initTrackListPage = function(name) {
  var caller = this;
  if (name == null) {
    name = caller.nameFromQueryParameter();
  }
  if (!this.token) {
    if (name) {
      window.location.href = "../login.html?from=" + encodeURIComponent("secure/tracks.html?name=" + name);
    } else {
      window.location.href = "../login.html?from=secure/tracks.html";
    }
  }
  var activity = caller.activityFromQueryParameter();
  $("#search_query").autocomplete({
    source : function(request, response) {
      caller.autocomplete(request, response, false);
    },
    minLength : 2,
    select : function(event, ui) {
      if (ui.item && ui.item.value) {
        caller.loadTrackList(ui.item.value);
        this.value = '';
      }
    },
    open : function() {
      $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
    },
    close : function() {
      $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
    }
  });
  this.showLoginButtons();
  this.loadTrackList(name, null, activity);
}

Client.prototype.initOverviewPage = function(name) {
  var caller = this;
  if (name == null) {
    name = caller.nameFromQueryParameter();
  }
  var activity = caller.activityFromQueryParameter();
  $("#pager-feature .fg-button:not(.ui-state-disabled)").hover(function() {
    $(this).addClass("ui-state-hover");
  }, function() {
    $(this).removeClass("ui-state-hover");
  }).mousedown(function() {
    $(this).addClass("ui-state-active");
  }).mouseup(function() {
    $(this).removeClass("ui-state-active");
  });
  $("#search_query").autocomplete({
    source : function(request, response) {
      caller.autocomplete(request, response, true);
    },
    minLength : 2,
    select : function(event, ui) {
      if (ui.item && ui.item.value) {
        caller.loadOverview(ui.item.value);
        this.value = '';
      }
    },
    open : function() {
      $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
    },
    close : function() {
      $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
    }
  });
  this.showLoginButtons();
  this.loadOverview(name, null, null, activity);
}

Client.prototype.initProfilePage = function() {
  var caller = this;
  $("#button_delete").click(function(e) {
    caller.makeUserDelete();
  });
  $("#profile_show_banner").click(function(e) {
    caller.showBanner = this.checked;
    caller.animateHeaderPhoto();
  });
  this.showLoginButtons();
  $('#profile_show_banner').prop("checked", this.showBanner);
  this.loadProfile();
}

Client.prototype.initAboutPage = function() {
  this.showLoginButtons();
}

Client.prototype.initMapPage = function(name) {
  var caller = this;
  if (name == null) {
    name = caller.nameFromQueryParameter();
  }
  var activity = caller.activityFromQueryParameter();
  $("#search_query").autocomplete({
    source : function(request, response) {
      caller.autocomplete(request, response, true);
    },
    minLength : 2,
    select : function(event, ui) {
      if (ui.item && ui.item.value) {
        caller.loadMap(ui.item.value);
        this.value = ui.item.value;
      }
    },
    open : function() {
      $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
    },
    close : function() {
      $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
    }
  });
  this.showLoginButtons();
  this.loadMap(name, activity);
}

// ---------------------------------------------------------------------

Client.prototype.messageOn = function(msg) {
  var message = $('#message');
  message[0].innerHTML = msg;
  message[0].className = 'messageOn';
}

Client.prototype.messageOff = function() {
  var message = $('#message');
  message[0].innerHTML = '';
  message[0].className = 'messageOff';
}

Client.prototype.statusOn = function(msg) {
  var caller = this;
  var options = {
    title: title_bar,
    tag: 'hikingtracks',
    body: msg,
    onclick: function () {
      caller.notification.close();
      caller.notification = null;
    }
  };
  $.notification(options).then(function (notification) {
    caller.notification = notification;
  }, function () {
    caller.notification = null;
    var status = $('#status');
    status[0].innerHTML = msg;
    status[0].className = 'statusOn';
  });
}

Client.prototype.errorStatusOn = function(msg) {
  var caller = this;
  var options = {
    title: title_bar,
    tag: 'hikingtracks',
    body: msg,
    onclick: function () {
      caller.notification.close();
      caller.notification = null;
    }
  };
  $.notification(options).then(function (notification) {
    caller.notification = notification;
  }, function () {
    caller.notification = null;
    var status = $('#status');
    status[0].innerHTML = msg;
    status[0].className = 'errorStatusOn';
  });
}

Client.prototype.statusOff = function() {
  var caller = this;
  if (caller.notification) {
    caller.notification.close();
  }
  var status = $('#status');
  status[0].innerHTML = '';
  status[0].className = 'statusOff';
}

Client.prototype.progressOn = function() {
  $('#progressbar').css('display', 'block');
}

Client.prototype.progressOff = function() {
  $('#progressbar').css('display', 'none');
}

// ---------------------------------------------------------------------

Client.prototype.showLoginButtons = function() {
  var caller = this;
  var login = $('#login');
  var logout = $('#logout');
  if (this.token) {
    logout.css('display', '');
    login.css('display', 'none');
  } else {
    login.css('display', '');
    logout.css('display', 'none');
  }
  $('.headerphoto').click(function() {
    caller.showBanner = false;
    $.cookie('show-banner', caller.showBanner, {
      path : '/', expires: 30
    });
    caller.animateHeaderPhoto();
  });
  if (!this.showBanner) {
    $('.headerphoto').css('height', '0px').css('opacity', 0.01);
  }
  this.localize('#languages', 'languages', {
    prefix : this.imagePath
  });
  $('#languages a').click(function() {
    caller.changeLanguage(this.id, true);
  });
  $("#current-language").click(function() {
    var show = $('#language').attr('show');
    if (show == 'true') {
      $("#language").attr('show', 'false').find('ul').slideUp();
    } else {
      $("#language").attr('show', 'true').find('ul').slideDown();
    }
  });
  $('nav').fadeIn(500);
}

Client.prototype.changeLanguage = function(language, update) {
  var caller = this;
  if (!language) {
    language = 'en';
  }
  var oldLanguage = $.cookie('language');
  $('#current-language img').attr('src', this.imagePath + '/' + language + '.png');
  $("#language").attr('show', 'false').find('ul').slideUp();
  if (language != oldLanguage) {
    $.cookie('language', language, {
      path : '/', expires: 365
    });
    if (update) {
      $('#body').fadeOut('slow', function() {
        if (caller.trackData && caller.trackData.name) {
          window.location.hash = caller.trackData.name;
        }
        window.location.reload();
      });
    }
  }
  return language;
}

// ---------------------------------------------------------------------

Client.prototype.showRedirectMessage = function(message) {
  if (message == null) {
    message = $.parseQuery().message;
  }
  if (message) {
    this.messageOn(message);
  }
}

// ---------------------------------------------------------------------

Client.prototype.initMessages = function(path, templates, imagePath) {
  // Configure jquery-Mustache to warn on missing templates (to aid debugging)
  $.Mustache.options.warnOnMissingTemplates = true;
  if (!templates) {
    templates = '../pages/templates.html';
  }
  if (!imagePath) {
    imagePath = '../images/flags';
  }
  this.imagePath = imagePath;
  $.Mustache.load(templates);
  var language = $.cookie('language');
  language = this.changeLanguage(language, false);
  $.i18n.properties({
    name : 'messages',
    path : path,
    mode : 'both',
    cache : true,
    language : language,
    callback : function() {
      $.Mustache.addFromDom('body');
      $('#body').mustache('body', $.i18n.map, {
        method : 'html'
      }).show();
    }
  });
}

// ---------------------------------------------------------------------

Client.prototype.getData = function(url, data) {
  var caller = this;
  caller.statusOff();
  $('#indicator').css('visibility', 'visible');
  var response = null;
  $.ajax({
    url : url,
    global : false,
    type : 'GET',
    dataType : 'json',
    data : data,
    async : false,
    headers : {
      'Cache-Control' : 'no-cache'
    },
    contentType : 'application/json',
    error : function(msg) {
      $('#indicator').css('visibility', 'hidden');
      log(msg);
    },
    success : function(data, status, xhr) {
      response = data;
      var etag = xhr.getResponseHeader("Etag");
      if (etag) {
        response.etag = etag;
      }
      $('#indicator').css('visibility', 'hidden');
      caller.messageOff();
    },
    statusCode : {
      401 : function(msg) {
        caller.messageOn(msg_access_denied);
        $.cookie('x-auth-token', null, {path:'/'});
        window.location.reload();
      },
      403 : function(msg) {
        caller.messageOn(msg_access_denied);
        $.cookie('x-auth-token', null, {path:'/'});
        window.location.reload();
      },
      404 : function(msg) {
        caller.messageOn(msg_not_found);
      },
      400 : function(msg) {
        caller.messageOn(msg.responseText);
      },
      500 : function(msg) {
        caller.messageOn(msg_internal_server_error);
      },
      503 : function(msg) {
        caller.messageOn(msg_service_temporarily_unavailable);
      }
    }
  });
  return response;
}

Client.prototype.postData = function(url, method, body, successMsg) {
  $('#indicator').css('visibility', 'visible');
  var caller = this;
  var etag = body.etag;
  delete body.etag;
  caller.statusOff();
  var response = null;
  $.ajax({
    url : url,
    global : false,
    type : method,
    data : $.toJSON(body),
    dataType : 'json',
    async : false,
    headers : {
      'Cache-Control' : 'no-cache'
    },
    contentType : 'application/json',
    beforeSend : function(xhr) {
      if (etag) {
        xhr.setRequestHeader("If-Match", etag);
      }
    },
    success : function(data, status, xhr) {
      response = data;
      var etag = xhr.getResponseHeader("Etag");
      if (etag) {
        response.etag = etag;
      }
      caller.messageOff();
      $('#indicator').css('visibility', 'hidden');
      if (successMsg) {
        caller.statusOn(successMsg);
      }
    },
    error : function(msg) {
      $('#indicator').css('visibility', 'hidden');
      log(msg);
    },
    statusCode : {
      401 : function(msg) {
        $.cookie('x-auth-token', null, {path:'/'});
        caller.token = null;
        caller.handlePostError(msg_access_denied, body, etag);
      },
      403 : function(msg) {
        $.cookie('x-auth-token', null, {path:'/'});
        caller.token = null;
        caller.handlePostError(msg_access_denied, body, etag);
      },
      404 : function(msg) {
        caller.handlePostError(msg_not_found, body, etag);
      },
      400 : function(msg) {
        caller.handlePostError(msg.responseText, body, etag);
      },
      409 : function(msg) {
        caller.handlePostError(msg.responseText, body, etag);
      },
      412 : function(msg) {
        caller.handlePostError(msg_precondition_failed, body, etag);
      },
      500 : function(msg) {
        caller.handlePostError(msg_internal_server_error, body, etag);
      },
      503 : function(msg) {
        caller.handlePostError(msg_service_temporarily_unavailable, body, etag);
      }
    }
  });
  document.body.style.cursor = "default";
  return response;
}

// ---------------------------------------------------------------------

Client.prototype.handlePostError = function(message, body, etag) {
  body.etag = etag;
  this.messageOff();
  this.errorStatusOn(message);
}

// ---------------------------------------------------------------------

Client.prototype.makeProfileUpdate = function() {
  $.cookie('show-banner', this.showBanner, {
    path : '/', expires: 30
  });
  if (this.profileData != null) {
    var userData = this.profileData;
    userData.name = getValue('profile_name');
    userData.email = getValue('profile_email');
    userData.published = getChecked('profile_published');
    this.messageOn(msg_sending);
    var response = this.postData(baseurl + serviceUserUrl, "PUT", this.profileData, msg_profile_updated);
    if (response) {
      this.profileData = response;
    }
  }
}

Client.prototype.loadProfile = function() {
  this.messageOn(msg_loading);
  this.profileData = this.getData(baseurl + serviceUserUrl);
  if (this.profileData != null) {
    var userData = this.profileData;
    setValue('profile_name', userData.name);
    setValue('profile_email', userData.email);
    setChecked('profile_published', userData.published);
    this.addAvatar(userData, '#avatar');
    this.messageOff();
  }
}

Client.prototype.addAvatar = function(user, image) {
  if (user) {
    var link = user.link;
    if (link && link.href) {
      $(image).attr('src', link.href);
    } else {
      $(image).attr('src', '../images/empty.png');
    }
  } else {
    $(image).attr('src', '../images/empty.png');
  }
}

Client.prototype.makeProfileDelete = function() {
  if (this.profileData != null) {
    this.messageOn(msg_deleting);
    this.postData(baseurl + serviceUserUrl, "DELETE", null, msg_profile_deleted);
    this.profileData = null;
    window.location.href = "../login.html";
  }
}

Client.prototype.newTrackData = function() {
  this.trackData = {
    id : null,
    name : '',
    location : '',
    date : null,
    description : '',
    trackdata : [],
    image : []
  };
}

Client.prototype.loadTrack = function(name) {
  if (name == null) {
    name = this.nameFromAnchor();
  }
  if (name) {
    this.messageOn(msg_loading);
    this.trackData = this.getData(baseurl + serviceTrackUrl + encodeURIComponent(name));
    if (this.trackData == null) {
      this.newTrackData();
      this.addTrackData();
      this.addImage();
    }
  } else {
    this.newTrackData();
    this.addTrackData();
    this.addImage();
  }
  this.showTrack(this.trackData);
}

Client.prototype.loadTrackDetail = function(name, showlink, updateHistory, includePublic) {
  if (name == null) {
    name = this.nameFromAnchor();
  }
  if (name) {
    this.messageOn(msg_loading);
    if (updateHistory && history.pushState) {
      history.pushState(name, title_bar, "detail.html#" + encodeURIComponent(name));
    }
    var data = {};
    data.public = includePublic;
    this.trackData = this.getData(baseurl + serviceTrackUrl + encodeURIComponent(name), data);
    if (this.trackData == null) {
      this.newTrackData();
    }
  } else {
    this.newTrackData();
    if (history.pushState) {
      history.pushState(null, title_bar, "detail.html");
    }
  }
  this.showTrackDetail(this.trackData, showlink);
}

Client.prototype.showTrack = function(trackData) {
  setValue('track_id', trackData.id);
  setValue('track_name', trackData.name);
  setValue('track_location', trackData.location);
  setValue('track_latitude', trackData.latitude);
  setValue('track_longitude', trackData.longitude);
  setValue('track_activity', trackData.activity);
  setChecked('track_published', trackData.published);
  $('#track_date').datepicker('setDate', trackData.date ? new Date(trackData.date) : null);
  $('#button_delete').attr("disabled", (trackData.id) ? false : true);
  $('#button_show').attr("disabled", (trackData.id) ? false : true);
  $('#imgupload').attr("disabled", (trackData.id) ? false : true);
  $('#kmlupload').attr("disabled", (trackData.id) ? false : true);
  $('#upload_hint').text((trackData.id) ? msg_upload_hint : msg_save_before_upload);
  setValue('track_description', trackData.description);
  $('#track-data-list').empty();
  $('#image-list').empty();
  if (trackData['trackdata']) {
    for (var i = 0; i < trackData['trackdata'].length; i++) {
      var td = trackData['trackdata'][i];
      if (td != null) {
        this.addTrackData(td, i);
      }
    }
  } else {
    trackData['trackdata'] = [];
  }
  if (trackData['image']) {
    for (var j = 0; j < trackData['image'].length; j++) {
      var img = trackData['image'][j];
      if (img != null) {
        this.addImage(img, j);
      }
    }
  } else {
    trackData['image'] = [];
  }
}

Client.prototype.showTrackDetail = function(trackData, showlink) {
  setValue('track_id', trackData.id);
  setValue('button_edit', btn_edit);
  $('#button_edit').css('display', trackData.readOnly ? 'none' : 'inherit');
  setValue('button_list', btn_back_to_overview);
  $('#track_name').empty();
  if (showlink) {
    this.localize('#track_name', 'detail-link', {
      link : encodeURIComponent(trackData.name),
      name : trackData.name
    });
  } else {
    setText('#track_name', trackData.name);
  }
  setText('#track_location', this.getLocationInfo(trackData));
  setText('#track_activity', trackData.activity);
  setFloat('#track_distance', trackData.distance);
  this.showUser('#track_author', trackData.user, showlink);
  this.addAvatar(trackData.user, '#avatar');
  setText('#track_description', trackData.description, true);
  setChecked('track_published', trackData.published);
  setDate('#track_date', trackData.date);
  $('#button_edit').attr("disabled", (trackData.id) ? false : true);
  this.showGoogleMaps('#google-maps', trackData);
  this.showElevationProfile('#elevation-chart', trackData);
  this.addCarousel("#image-parent", trackData);
  this.addNavigationDetailLink(trackData, '#prev_feature', 'previous', showlink);
  this.addNavigationDetailLink(trackData, '#next_feature', 'next', showlink);
}

Client.prototype.showUser = function(id, user, showlink) {
  $(id).empty();
  if (user) {
    var email = user.email;
    var name = user.name;
    if (!name) {
      name = email;
    }
    this.localize(id, showlink ? 'author-from' : 'author-show', {
      email : email,
      name : name
    });
  } else if (!showlink) {
    $(id).text(txt_author_anon);
  }
}

Client.prototype.addNavigationDetailLink = function(trackData, item, rel, showlink) {
  var elem = this.getLink(trackData, rel);
  $(item).css('display', (elem != null) ? 'inherit' : 'none');
  if (elem != null) {
    var name = elem.value;
    $(item).off("click").click(function(event) {
      event.preventDefault();
      client.loadTrackDetail(name, showlink, true);
    });
  }
}

Client.prototype.addCarousel = function(elem, trackData) {
  var hasImages = trackData['image'] && trackData['image'].length > 0;
  if (hasImages) {
    var imageCount = trackData['image'].length;
    if (this.initCarousel) {
      $(elem).empty().mustache('image-list', $.i18n.map);
    }
    $(elem).css('display', 'inherit');
    for (var i = 0; i < imageCount; i++) {
      var img = trackData['image'][i];
      if (img != null) {
        this.showImage('#image-list', img.name, img, i, "SMALL");
      }
    }
    var maxVisible = Math.floor($('#main').outerWidth() / 160);
    $('#image-list').slick({
      infinite: (imageCount >= maxVisible),
      slidesToShow: 1,
      variableWidth: true,
      swipeToSlide: true,
      centerMode: false,
      draggable: false,
      dots: (imageCount >= maxVisible),
      arrows: (imageCount >= maxVisible)
    });
    $('#image-list').Chocolat({
      loop: true,
      imageSize: 'contain'
    });
    this.initCarousel = true;
  } else {
    this.initCarousel = false;
    $(elem).css('display', 'none').empty().mustache('image-list', $.i18n.map);
  }
}

Client.prototype.getLink = function(trackData, rel) {
  var links = trackData.link;
  if (links) {
    for (var i = 0; i < links.length; i++) {
      var link = links[i];
      if (link.rel && link.rel == rel) {
        return link;
      }
    }
  }
  return null;
}

Client.prototype.makeTrackUpdate = function() {
  if (this.trackData != null) {
    var oldData = this.trackData;
    var trackData = this.trackData;
    var currentName = trackData.name;
    trackData.name = getValue('track_name');
    trackData.location = getValue('track_location');
    trackData.longitude = getValue('track_longitude');
    trackData.latitude = getValue('track_latitude');
    trackData.activity = getValue('track_activity');
    trackData.date = $('#track_date').datepicker('getDate');
    if (trackData.date) {
      trackData.date = printDate(trackData.date);
    }
    trackData.description = getValue('track_description');
    trackData.published = getChecked('track_published');
    trackData.id = getValue('track_id');
    var index = 0;
    for (var i = 0; i < trackData['trackdata'].length; i++) {
      var td = trackData['trackdata'][i];
      if (td) {
        td.name = getValue('track_data_name_' + index);
        td.url = getValue('track_data_url_' + index);
      } else {
        trackData['trackdata'].splice(i, 1);
        i--;
      }
      index++;
    }
    index = 0;
    for (var j = 0; j < trackData['image'].length; j++) {
      var img = trackData['image'][j];
      if (img) {
        img.name = getValue('image_name_' + index);
        img.url = getValue('image_url_' + index);
      } else {
        trackData['image'].splice(j, 1);
        j--;
      }
      index++;
    }
    this.messageOn(msg_sending);
    if (trackData.id != null) {
      this.trackData = this.postData(baseurl + serviceTrackUrl + encodeURIComponent(currentName), "PUT",
        this.trackData, msg_track_updated);
    } else {
      this.trackData = this.postData(baseurl + serviceTrackUrl, "POST", this.trackData, msg_track_added);
    }
    if (this.trackData == null) {
      this.trackData = oldData;
    }
    history.pushState(name, title_bar, "track.html#" + encodeURIComponent(this.trackData.name));
    this.showTrack(this.trackData);
  }
}

Client.prototype.makeTrackDelete = function() {
  if (this.trackData != null) {
    var trackData = this.trackData;
    trackData.name = getValue('track_name');
    trackData.published = getChecked('track_published');
    trackData.id = getValue('track_id');
    if (trackData.id != null) {
      this.messageOn(msg_deleting);
      this.postData(baseurl + serviceTrackUrl + encodeURIComponent(trackData.name), "DELETE", this.trackData,
          msg_track_deleted);
      this.newTrackData();
      $('#image-list').empty();
      $('#track-data-list').empty();
      this.showTrack(this.trackData);
    }
  }
}

Client.prototype.showTrackDetails = function(trackData) {
  if (trackData != null) {
    window.location.href = "../pages/detail.html#" + encodeURIComponent(trackData.name);
  }
}

Client.prototype.editTrack = function(trackData) {
  if (trackData != null && trackData.name) {
    window.location.href = "../secure/track.html#" + encodeURIComponent(trackData.name);
  }
}

Client.prototype.showTrackList = function(name, activity) {
  var suffix = "";
  suffix = addQueryParam(suffix, "name", name);
  suffix = addQueryParam(suffix, "activity", activity);
  var url;
  if (this.token) {
    url = "../secure/tracks.html" + suffix;
  } else {
    url = "../pages/index.html" + suffix;
  }
  window.location.href = url;
}

Client.prototype.addTrackData = function(td, index) {
  if (this.trackData != null) {
    var trackData = this.trackData;
    if (td == null) {
      index = trackData['trackdata'].length;
      td = {
        'name': null,
        'url': null
      };
      trackData['trackdata'][index] = td;
    }
    var isNew = (td.url == null);
    if ($('#track_data_name_' + index).length == 0) {
      this.localize('#track-data-list', isNew ? 'add-track-data-new' : 'add-track-data', {
        index : index
      });
      $('#button_delete_track_data_' + index).click(function(e) {
        client.deleteTrackData(index)
      });
    }
    setValue('track_data_name_' + index, td.name);
    if (td.url) {
      setValue('track_data_url_' + index, td.url);
      $('#track_data_url_' + index).attr('disabled', true);
    }
  }
}

Client.prototype.addImage = function(img, index) {
  var caller = this;
  if (this.trackData != null) {
    var trackData = this.trackData;
    var isLast = (index == trackData['image'].length - 1);
    if (img == null) {
      index = trackData['image'].length;
      img = {
        'name': null,
        'url': null
      };
      trackData['image'][index] = img;
      isLast = true;
    }
    var isNew = (img.url == null);
    var isFirst = (index == 0);
    if ($('#image_name_' + index).length == 0) {
      this.localize('#image-list', isNew ? 'add-image-new' : 'add-image', {
        index : index,
        url : img.url
      });
      $('#button_delete_image_' + index).click(function(e) {
        client.deleteImage(index)
      });
    }
    if (isLast) {
      $('#button_down_image_' + index).css('visibility', 'hidden');
    }
    if (isFirst) {
      $('#button_up_image_' + index).css('visibility', 'hidden');
    }
    $('#button_down_image_' + index).click(function(e) {
      caller.moveImage(trackData, index, index + 1);
      return false;
    });
    $('#button_up_image_' + index).click(function(e) {
      caller.moveImage(trackData, index, index - 1);
      return false;
    });
    this.setImageValues(img, index);
  }
}

Client.prototype.setImageValues = function(img, index) {
  setValue('image_name_' + index, img.name);
  if (img.url) {
    setValue('image_url_' + index, img.url);
    $('#image_url_' + index).attr('disabled', true);
    $('#image_img_' + index).attr('alt', img.name);
    $('#image_img_' + index).attr('src', img.url + '?thumbnail=SMALL');
  }
}

Client.prototype.moveImage = function(trackData, fromIndex, toIndex) {
  var images = trackData['image'];
  if (fromIndex < 0 || toIndex < 0 || fromIndex >= images.length || toIndex >= images.length) {
    return;
  }
  var temp = images[toIndex];
  if (temp == null) {
    this.moveImage(trackData, fromIndex, toIndex + (toIndex - fromIndex));
    return;
  }
  images[toIndex] = images[fromIndex];
  images[fromIndex] = temp;
  this.setImageValues(images[fromIndex], fromIndex);
  this.setImageValues(images[toIndex], toIndex);
}

Client.prototype.showImage = function(elem, name, img, index, type) {
  if ($('#image_name_' + index).length == 0) {
    var url = img.url.split("?")[0];
    var hrefUrl = url + "?thumbnail=LARGE";
    if (type) {
      url = url + "?thumbnail=" + type;
    }
    $(elem).mustache('show-image', $.extend({
      index: index,
      name: name,
      hrefUrl: hrefUrl,
      url: url
    }, $.i18n.map));
  }
}

Client.prototype.showGoogleMaps = function(elem, trackData) {
  var trackDataList = trackData['trackdata'];
  var hasCoordinates = trackData.latitude && trackData.longitude;
  var hasTrackData = trackDataList && (trackDataList.length > 0);
  if (!hasTrackData && !hasCoordinates) {
    $(elem).css('display', 'none');
    return;
  }
  var caller = this;
  var center = null;
  var isMapLoaded = false;
  $(elem).css("display", "block");
  if (hasCoordinates) {
    center = new google.maps.LatLng(trackData.latitude, trackData.longitude);
    $(elem).css("visibility", "visible");
  } else {
    $(elem).css("visibility", "hidden");
  }
  if (!this.map) {
    var mapOptions = {
      disableDefaultUI : true,
      navigationControl : true,
      mapTypeControl : true,
      scaleControl : true,
      center : center,
      zoom : 12,
      mapTypeId : google.maps.MapTypeId.TERRAIN
    };
    var mapContainer = $(elem).get(0);
    this.map = new google.maps.Map(mapContainer, mapOptions);
    if (this.supportsFullScreen(mapContainer)) {
      var controlDiv = document.createElement('div');
      this.addGoogleMapsControls(controlDiv, this.map);
      controlDiv.index = 1;
      this.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(controlDiv);
    }
  } else {
    if (this.kmlArray) {
      for (var i=0; i<this.kmlArray.length; i++) {
        this.kmlArray[i].setMap(null);
      }
    }
    if (this.markers) {
      for (var j=0; j<this.markers.length; j++) {
        this.markers[j].setMap(null);
      }
    }
    if (center) {
      this.map.setCenter(center);
      this.map.setZoom(12);
    }
  }
  this.kmlArray = [];
  this.markers = [];
  if (hasTrackData) {
    var clickListener = function(e) {
      caller.statusOn(e.featureData.description);
    };
    var metadataChangeListener = function() {
       // maybe use also defaultviewport_changed
       $(elem).css("visibility", "visible");
       isMapLoaded = true;
    }

    for (var k = 0; k < trackDataList.length; k++) {
      var td = trackDataList[k];
      if (td != null) {
        var url = td.url;
        if (this.token) {
          url = url + "?x-auth-token=" + encodeURIComponent(caller.token);
        }
        var kmlLayer = new google.maps.KmlLayer(url, {
          suppressInfoWindows : true
        });

        google.maps.event.addListener(kmlLayer, 'click', clickListener);
        google.maps.event.addListener(kmlLayer, 'metadata_changed', metadataChangeListener);
        kmlLayer.setMap(this.map);
        this.kmlArray.push(kmlLayer);
        if (td.highestPoint) {
          var p1 = new google.maps.LatLng(td.highestPoint.lat, td.highestPoint.lng)
          if (!center) {
            center = p1;
            this.map.setCenter(p1);
          }
          var m1 = new google.maps.Marker({
            position : p1,
            label : '★',
            map : this.map,
            title : lbl_Elevation + ": " + td.highestPoint.elevation
          });
          this.markers.push(m1);
        }
        if (td.lowestPoint) {
          var p2 = new google.maps.LatLng(td.lowestPoint.lat, td.lowestPoint.lng)
          if (!center) {
            center = p2;
            this.map.setCenter(p2);
          }
          var m2 = new google.maps.Marker({
            position : p2,
            label : '▲',
            map : this.map,
            title : lbl_Elevation + ": " + td.lowestPoint.elevation
          });
          this.markers.push(m2);
        }
      }
    }
    if (!center) {
      window.setTimeout(function() {
        if (!isMapLoaded) {
          caller.messageOn(msg_google_maps_timeout);
          $(elem).css("display", "none");
        }
      }, 2500);
    } else {
      $(elem).css("visibility", "visible");
    }
  }
}

Client.prototype.toggleFullscreenGoogleMaps = function(close) {
  if (this.isFullScreen()) {
    this.exitFullScreen();
  } else {
    this.requestFullScreen(document.getElementById('google-maps'));
  }
}

Client.prototype.handleFullscreenGoogleMaps = function(close) {
  var trackData = this.trackData;
  var hasImages = trackData && trackData['image'] && trackData['image'].length > 0;
  if (!this.isFullScreen()) {
    if (hasImages) {
      this.map.controls[google.maps.ControlPosition.BOTTOM_CENTER].pop();
    }
    if (trackData) {
      this.map.controls[google.maps.ControlPosition.RIGHT_CENTER].pop();
    }
    $('#google-maps').css('height', '450px');
    google.maps.event.trigger(this.map, 'resize');
  } else {
    $('#google-maps').css('height', '100%');
    google.maps.event.trigger(this.map, 'resize');
    if (hasImages) {
      var imageCount = trackData['image'].length;
      var controlUI = document.createElement('div');
      controlUI.className = 'google-maps-fullscreen-carousel google-maps-control carousel';
      this.map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(controlUI);
      for (var i = 0; i < imageCount; i++) {
        var img = trackData['image'][i];
        if (img != null) {
          var url = img.url + "?thumbnail=SMALL";
          $('.google-maps-fullscreen-carousel').mustache('show-googlemaps-image', $.extend({
            index : i,
            name : img.name,
            url : url
          }, $.i18n.map));
        }
      }
      var maxVisible = Math.floor($('#google-maps').outerWidth() / 160);
      $('.google-maps-fullscreen-carousel').slick({
        infinite: (imageCount >= maxVisible),
        slidesToShow: 1,
        centerMode: false,
        variableWidth: true,
        swipeToSlide: true,
        draggable: false,
        dots: (imageCount >= maxVisible),
        arrows: (imageCount >= maxVisible)
      });
    }
    this.addGoogleMapsDetail(trackData);
  }
}

Client.prototype.addGoogleMapsControls = function(controlDiv, map) {
  controlDiv.style.paddingTop = '5px';

  var controlUI = document.createElement('div');
  controlUI.className = 'google-maps-control';
  controlUI.title = title_fullscreen;
  controlDiv.appendChild(controlUI);

  var controlText = document.createElement('div');
  controlText.className = 'google-maps-control-btn';
  controlText.innerHTML = btn_fullscreen;
  controlUI.appendChild(controlText);
  var caller = this;
  google.maps.event.addDomListener(controlUI, 'click', function() {
    caller.toggleFullscreenGoogleMaps();
  });
  this.handleFullScreenChange(function(e) {
    caller.handleFullscreenGoogleMaps();
  });
}

Client.prototype.addGoogleMapsDetail = function(trackData) {
  if (!trackData) {
    return;
  }
  var controlUI = $('<div class="google-maps-control google-maps-post"></div>');
  controlUI.mustache('gtrack-detail', $.i18n.map);
  this.map.controls[google.maps.ControlPosition.RIGHT_CENTER].push(controlUI.get(0));

  setValue('gtrack_id', trackData.id);
  setText('#gtrack_name', trackData.name);
  setText('#gtrack_location', this.getLocationInfo(trackData));
  setText('#gtrack_activity', trackData.activity);
  setFloat('#gtrack_distance', trackData.distance);
  setChecked('gtrack_published', trackData.published);
  setDate('#gtrack_date', trackData.date);
  this.showUser('#gtrack_author', trackData.user);
}

Client.prototype.deleteTrackData = function(index) {
  $('#td_' + index).remove();
  if (this.trackData != null) {
    var trackData = this.trackData;
    trackData['trackdata'][index] = null;
  }
}

Client.prototype.deleteImage = function(index) {
  $('#img_' + index).remove();
  if (this.trackData != null) {
    var trackData = this.trackData;
    trackData['image'][index] = null;
  }
}

Client.prototype.loadTrackList = function(search, start, activity) {
  this.messageOn(msg_loading);
  var data = {};
  data.max = this.maxResults;
  data.public = false;
  if (start) {
    data.start = start;
  }
  if (search) {
    data.name = search;
  }
  if (activity) {
    data.activity = activity;
  }
  this.trackData = this.getData(baseurl + serviceTrackUrl, data);
  var suffix = "";
  suffix = addQueryParam(suffix, "name", search);
  suffix = addQueryParam(suffix, "activity", activity);
  if (history.pushState) {
    history.pushState(search, title_bar, "tracks.html" + suffix);
  }
  if (this.trackData != null) {
    $('#track-list').empty();
    var result = this.trackData.track;
    if (!result || result.length == 0) {
      this.showEmptyTrackList();
    } else {
      for (var i = 0; i < result.length; i++) {
        this.showTrackInfo(result[i], i);
      }
    }
    this.showPaging(this.trackData, false, search, start, activity);
  } else {
    this.showEmptyTrackList();
  }
}

Client.prototype.loadOverview = function(search, start, ontop, activity) {
  var caller = this;
  this.messageOn(msg_loading);
  var data = {};
  data.max = this.maxResults;
  data.public = true;
  if (start) {
    data.start = start;
  }
  if (search) {
    data.name = search;
  }
  if (activity) {
    data.activity = activity;
  }
  if (!ontop) {
    ontop = 0;
  }
  if (!start) {
    start = 0;
  }
  caller.trackList = caller.getData(baseurl + serviceTrackUrl, data);
  var suffix = "";
  suffix = addQueryParam(suffix, "name", search);
  suffix = addQueryParam(suffix, "activity", activity);
  if (history.pushState) {
    history.pushState(search, title_bar, "index.html" + suffix);
  }
  if (caller.trackList != null) {
    $('#track-list').empty();
    data = caller.trackList;
    var result = data.track;
    if (!result || result.length == 0) {
      caller.showEmptyTrackList();
    } else {
      var total = parseInt(data.total, 10);
      $('#featured_track').css('display', 'block');
      if (result.length > 1) {
        caller.localize('#track-list', 'track-list-post-more-tracks', {
          msg_more_tracks : msg_more_tracks
        });
      } else {
        $('#track-list').empty();
      }
      caller.loadTrackDetail(result[ontop].name, true, false, true);
      for (var i = 0; i < result.length; i++) {
        if (i != ontop) {
          var trackData = result[i];
          caller.showTrackSummaryInfo(trackData, i);
        }
      }
      caller.addNavigationButtons(result, total, search, start, ontop, activity);
      caller.showPaging(data, true, search, start, activity);
    }
  } else {
    caller.showEmptyTrackList();
  }
}

Client.prototype.loadMap = function(search, activity) {
  var caller = this;
  caller.messageOn(msg_loading);
  var data = {};
  data.max = 20;
  if (search) {
    data.name = search;
  }
  if (activity) {
    data.activity = activity;
  }
  if (caller.center) {
    caller.showOverviewMap('#google-maps', data, caller.center.lat(), caller.center.lng());
  } else {
    caller.showOverviewMap('#google-maps', data, null, null);
    caller.getCurrentPosition(function(latitude, longitude) {
      caller.showOverviewMap('#google-maps', data, latitude, longitude);
    });
  }
}

Client.prototype.addNavigationButtons = function(tracks, total, search, start, ontop, activity) {
  var caller = this;
  var hasMultiple = (tracks.length > 0);
  $('#prev_feature').css('display', hasMultiple ? 'inherit' : 'none');
  $('#next_feature').css('display', hasMultiple ? 'inherit' : 'none');
  if (hasMultiple) {
    $('#prev_feature').off("click").click(
        function(event) {
          event.preventDefault();
          if (ontop == 0) {
            var lastPage = Math.floor(total / caller.maxResults) * caller.maxResults;
            var lastIndex = total - lastPage - 1;
            caller.loadOverview(search, (start - caller.maxResults < 0) ? lastPage : (start - caller.maxResults),
                (start - caller.maxResults < 0) ? lastIndex : caller.maxResults - 1, activity);
          } else {
            caller.loadOverview(search, start, ontop - 1, activity);
          }
        });
    $('#next_feature').off("click").click(
        function(event) {
          event.preventDefault();
          if (ontop == tracks.length - 1) {
            caller.loadOverview(search, (start + caller.maxResults >= total) ? 0 : start + caller.maxResults, null,
                activity);
          } else {
            caller.loadOverview(search, start, ontop + 1, activity);
          }
        });
  }
}

Client.prototype.showEmptyTrackList = function() {
  this.localize('#track-list', 'track-list-post-no-tracks', {
    msg_no_tracks_found : msg_no_tracks_found
  });
  $('#featured_track').css('display', 'none');
  $('#prev_feature').css('display', 'none');
  $('#next_feature').css('display', 'none');
}

Client.prototype.getLocationInfo = function(trackData) {
  var location = '';
  if (trackData.location) {
    location += trackData.location;
  }
  if (trackData.latitude && trackData.longitude) {
    if (location.length > 0) {
      location += ' [';
    } else {
      location += '[';
    }
    location += this.decimalLatToDMS(trackData.latitude) + ', ';
    location += this.decimalLongToDMS(trackData.longitude);
    location += ']';
  }
  if (location.length == 0) {
    location = '-';
  }
  return location;
}

Client.prototype.showTrackInfo = function(trackData, index) {
  var caller = this;
  this.localize('#track-list', 'track-info', {
    token : this.token,
    index : index
  });
  setText('#track_name_' + index, trackData.name);
  setValue('track_id_' + index, trackData.id);
  setText('#track_location_' + index, this.getLocationInfo(trackData));
  this.showUser('#track_author_' + index, trackData.user);
  setText('#track_activity_' + index, trackData.activity);
  setText('#track_description_' + index, trackData.description, true);
  if (this.token) {
    setChecked('track_published_' + index, trackData.published);
  }
  setDate('#track_date_' + index, trackData.date);
  if (trackData.image && trackData.image.length > 0) {
    this.localize('#track_image_' + index, 'image', {
      name : trackData.image[0].name,
      url : trackData.image[0].url,
      track_name: encodeURIComponent(trackData.name)
    });
  }
  $("#button_show_" + index).click(function(e) {
    caller.showTrackDetails(trackData);
  });
  if (this.token) {
    $("#button_edit_" + index).click(function(e) {
      caller.editTrack(trackData);
    });
  } else {
    $("#button_edit_" + index).remove();
  }
}

Client.prototype.showTrackSummaryInfo = function(trackData, index) {
  var caller = this;
  caller.localize('#track-list-post', 'summary-info', {
    name : encodeURIComponent(trackData.name),
    index : index
  });
  setText('#track_name_' + index, trackData.name);
  setValue('track_id_' + index, trackData.id);
  setText('#track_location_' + index, this.getLocationInfo(trackData));
  caller.showUser('#track_author_' + index, trackData.user, false);
  caller.addAvatar(trackData.user, '#avatar_' + index);
  setText('#track_activity_' + index, trackData.activity);
  setFloat('#track_distance_' + index, trackData.distance);
  setText('#track_description_' + index, trackData.description, true);
  setDate('#track_date_' + index, trackData.date);
  if (trackData.image && trackData.image.length > 0) {
    caller.localize('#track_image_' + index, 'image', {
      name : trackData.image[0].name,
      url : trackData.image[0].url,
      track_name: encodeURIComponent(trackData.name)
    });
  }
}

Client.prototype.showPaging = function(trackList, global, search, current, activity) {
  $('#pager').css('display', 'none');
  $('#pager-list').empty();
  if (trackList) {
    var total = trackList.total;
    var start = trackList.start;
    var size = 0;
    if (trackList.track) {
      size = trackList.track.length;
    }
    if ((total != null) && (start != null) && (total > size)) {
      if (!current) {
        current = 0;
      }
      var count = current - Math.floor(this.maxPagers / 2) * this.maxResults;
      if (count < 0) {
        count = 0;
      }
      var pagers = 0;
      var currentIndex = current + 1;
      while (count < total && pagers < this.maxPagers) {
        pagers++;
        var firstIndex = count + 1;
        var lastIndex = count + this.maxResults;
        if (lastIndex > total) {
          lastIndex = total;
        }
        var pager = "&lt;" + firstIndex + " &hellip; " + lastIndex + "&gt;";
        if (firstIndex == lastIndex) {
          pager = "&lt;" + firstIndex + "&gt";
        }
        var itemId = (currentIndex >= firstIndex && currentIndex <= lastIndex);
        this.localize('#pager-list', 'pager', {
          count : count,
          pager : pager,
          itemId : itemId
        });
        $('#pager_' + count + ' a').attr('count', count);
        $('#pager_' + count + ' a').bind('click', function(event) {
          if (global) {
            client.loadOverview(search, parseInt($(this).attr('count'), 10), activity);
          } else {
            client.loadTrackList(search, parseInt($(this).attr('count'), 10), activity);
          }
        });
        count += this.maxResults;
      }
      $('#pager').css('display', 'block');
    }
  }
}

Client.prototype.searchTracks = function() {
  var text = getValue('search_query');
  var activity = getValue('search_activity');
  $("#search_query").autocomplete("close");
  this.loadTrackList(text, null, activity);
  setValue('search_query', '');
}

Client.prototype.searchOverview = function() {
  var text = getValue('search_query');
  var activity = getValue('search_activity');
  $("#search_query").autocomplete("close");
  this.loadOverview(text, null, null, activity);
  setValue('search_query', '');
}

Client.prototype.searchAndShowTracks = function() {
  var text = getValue('search_query');
  var activity = getValue('search_activity');
  $("#search_query").autocomplete("close");
  this.showTrackList(text, activity);
  setValue('search_query', '');
}

Client.prototype.searchMap = function() {
  var text = getValue('search_query');
  var activity = getValue('search_activity');
  $("#search_query").autocomplete("close");
  this.loadMap(text, activity);
}

// --------------------------------------------------
Client.prototype.initHeaderPhoto = function() {
  var img = Math.ceil(Math.random() * 3);
  $('.headerphoto').addClass('headerphoto-img' + img);
}
// --------------------------------------------------
Client.prototype.NORTH = 'N';
Client.prototype.SOUTH = 'S';
Client.prototype.EAST = 'E';
Client.prototype.WEST = 'W';
// --------------------------------------------------
Client.prototype.decimalToDMS = function(location, hemisphere) {
  if (location < 0) {
    location *= -1; // strip dash '-'
  }

  var degrees = Math.floor(location); // strip decimal remainer for degrees
  var minutesFromRemainder = (location - degrees) * 60; // multiply the remainer
  // by 60
  var minutes = Math.floor(minutesFromRemainder); // get minutes from integer
  var secondsFromRemainder = (minutesFromRemainder - minutes) * 60; // multiply
  // the
  // remainer
  // by 60
  var seconds = this.roundToDecimal(secondsFromRemainder, 2); // get minutes by
  // rounding to
  // integer
  return degrees + '°' + minutes + "'" + seconds + '"' + hemisphere;
}

Client.prototype.decimalLatToDMS = function(location) {
  // south if negative
  var hemisphere = (location < 0) ? this.SOUTH : this.NORTH;
  return this.decimalToDMS(location, hemisphere);
}

Client.prototype.decimalLongToDMS = function(location) {
  // west if negative
  var hemisphere = (location < 0) ? this.WEST : this.EAST;
  return this.decimalToDMS(location, hemisphere);
}

Client.prototype.roundToDecimal = function(inputNum, numPoints) {
  var multiplier = Math.pow(10, numPoints);
  return Math.round(inputNum * multiplier) / multiplier;
}
// --------------------------------------------------
Client.prototype.lookupCoordinates = function() {
  var caller = this;
  var text = getValue('track_location');
  if (text) {
    caller.messageOn(msg_loading);
    var geocoder = new google.maps.Geocoder();
    geocoder.geocode({
      'address' : text
    }, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        var location = results[0].geometry.location;
        setValue('track_latitude', location.lat().toFixed(6));
        setValue('track_longitude', location.lng().toFixed(6));
      } else {
        caller.messageOn(msg_geocode_error(status));
      }
      caller.messageOff();
    });
  } else if (navigator.geolocation) {
    caller.messageOn(msg_loading);
    navigator.geolocation.getCurrentPosition(function(position) {
      caller.messageOff();
      var location = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
      setValue('track_latitude', location.lat().toFixed(6));
      setValue('track_longitude', location.lng().toFixed(6));
      document.body.style.cursor = 'default';
    }, function(error) {
      caller.messageOn(msg_geocode_error(error.code));
    });
  }
}
// --------------------------------------------------
Client.prototype.autocomplete = function(request, response, onlyPublic) {
  $.ajax({
    url : baseurl + serviceTrackUrl,
    global : false,
    type : 'GET',
    dataType : 'json',
    data : {
      'name' : request.term,
      'max' : 10,
      'public' : onlyPublic,
      'thumbnail' : 'NONE'
    },
    headers : {
      'Cache-Control' : 'no-cache'
    },
    contentType : 'application/json',
    error : function(msg) {
      log(msg);
    },
    success : function(data) {
      var result = data;
      if (!result || !result.track) {
        return response([ {
          label : msg_nothing_found,
          value : null
        } ]);
      }
      response($.map(result.track, function(item) {
        return {
          label : item.name + (item.location ? ", " + item.location : ""),
          value : item.name
        }
      }));
    }
  });
}

// --------------------------------------------------

Client.prototype.requestFullScreen = function(elem) {
  if (elem.requestFullScreen) {
    elem.requestFullScreen();
  } else if (elem.mozRequestFullScreen) {
    elem.mozRequestFullScreen();
  } else if (elem.webkitRequestFullScreen) {
    elem.webkitRequestFullScreen();
  }
}

Client.prototype.supportsFullScreen = function(elem) {
  return elem.requestFullScreen || elem.mozRequestFullScreen || elem.webkitRequestFullScreen;
}

Client.prototype.isFullScreen = function() {
  return document.fullScreen || document.webkitIsFullScreen || document.mozFullScreen;
}

Client.prototype.exitFullScreen = function() {
  if (document.exitFullScreen) {
    document.exitFullScreen();
  } else if (document.mozCancelFullScreen) {
    document.mozCancelFullScreen();
  } else if (document.webkitCancelFullScreen) {
    document.webkitCancelFullScreen();
  }
}

Client.prototype.handleFullScreenChange = function(handler) {
  document.addEventListener("mozfullscreenchange", function(e) {
    handler();
  }, true);
  document.addEventListener("fullscreenchange", function() {
    handler();
  }, true);
  document.addEventListener("webkitfullscreenchange", function() {
    handler();
  }, true);
}

Client.prototype.localize = function(elem, template, params) {
  $(elem).mustache(template, $.extend(params, $.i18n.map));
}

Client.prototype.initOpenID = function() {
  var caller = this;
  $('#btn_google').click(function(e) {
    caller.login('GOOGLE');
  });
}

Client.prototype.login = function(app) {
  $('#openid_identifier').val(app);
  document.forms['login_form'].submit();
}

Client.prototype.nameFromAnchor = function() {
  var name = $(location).attr('hash');
  if (name) {
    return decodeURIComponent(name.substring(1));
  } else {
    return null;
  }
}

Client.prototype.nameFromQueryParameter = function() {
  var name = $.parseQuery().name;
  if (name) {
    return decodeURIComponent(name);
  } else {
    return null;
  }
}

Client.prototype.activityFromQueryParameter = function() {
  var activity = $.parseQuery().activity;
  if (activity) {
    return decodeURIComponent(activity);
  } else {
    return null;
  }
}

Client.prototype.animateHeaderPhoto = function() {
  if (this.showBanner) {
    $('.headerphoto').animate({
      opacity : 1,
      height : '200px'
    }, 3500);
  } else {
    $('.headerphoto').animate({
      opacity : 0.01,
      height : '0px'
    }, 3500);
  }
}

Client.prototype.showElevationProfile = function(elem, trackData) {
  if (!google.visualization) {
    return;
  }
  var trackDataList = trackData['trackdata'];
  var hasTrackData = trackDataList && (trackDataList.length > 0);
  var latlngs = [];
  var lowestPoint = null;
  var highestPoint = null;
  if (hasTrackData) {
    for (var i = 0; i < trackDataList.length; i++) {
      var td = trackDataList[i];
      if (td.samples != null) {
        for (var j = 0; j < td.samples.length; j++) {
          latlngs.push(td.samples[j]);
          if (lowestPoint == null || lowestPoint.elevation > td.samples[j].elevation) {
            lowestPoint = td.samples[j];
          }
          if (highestPoint == null || highestPoint.elevation < td.samples[j].elevation) {
            highestPoint = td.samples[j];
          }
        }
        if (!td.lowestPoint) {
          td.lowestPoint = lowestPoint;
        }
        if (!td.highestPoint) {
          td.highestPoint = highestPoint;
        }
      }
    }
  }

  if (latlngs.length == 0) {
    $(elem).css('display', 'none');
    $('#elevation-legend').css('display', 'none');
    return;
  }
  $(elem).css("display", "block");
  if (td && td.lowestPoint && td.highestPoint) {
    $('#elevation-low').text(txt_elevation_low(td.lowestPoint.elevation));
    $('#elevation-high').text(txt_elevation_high(td.highestPoint.elevation));
    $('#elevation-legend').css('display', 'block');
  } else {
    $('#elevation-legend').css('display', 'none');
  }

  var width = $(elem).width();
  var height = width / 4;
  var chartContainer = $(elem).get(0);

  var chart = new google.visualization.LineChart(chartContainer);
  var data = new google.visualization.DataTable();
  data.addColumn('number', lbl_Distance);
  data.addColumn('number', lbl_Elevation);
  data.addColumn({
    type : 'string',
    role : 'style'
  });

  for (var k = 0; k < latlngs.length; k++) {
    var style = null;
    if (lowestPoint && lowestPoint.elevation == latlngs[k].elevation) {
      style = 'point { size: 10; shape-type: triangle; fill-color: #45742A; }';
    } else if (highestPoint && highestPoint.elevation == latlngs[k].elevation) {
      style = 'point { size: 10; shape-type: star; fill-color: #272727; }';
    }
    data.addRow([latlngs[k].distance, latlngs[k].elevation, style]);
  }

  chart.draw(data, {
    width : width,
    height : height,
    legend : 'none',
    curveType : 'function',
    titleY : lbl_Elevation,
    titleX : lbl_Distance,
    colors : [ '#65944A' ],
    focusBorderColor : '#A52714',
    dataOpacity : 0.7,
    pointSize : 1
  });
}

Client.prototype.getCurrentPosition = function(callback) {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      if (position.coords.latitude && position.coords.longitude) {
        callback(position.coords.latitude, position.coords.longitude);
      }
    });
  }
}

Client.prototype.showOverviewMap = function(elem, data, latitude, longitude) {
  var caller = this;
  this.center = null;
  var hasCoordinates = latitude && longitude;
  if (hasCoordinates) {
    this.center = new google.maps.LatLng(latitude, longitude);
  }
  this.data = data;
  $(elem).css("display", "block");
  if (!this.map) {
    var mapOptions = {
      disableDefaultUI : false,
      navigationControl : true,
      mapTypeControl : true,
      scaleControl : true,
      center : (hasCoordinates ? caller.center : new google.maps.LatLng(0, 0)),
      zoom : (hasCoordinates ? 8 : 2),
      mapTypeId : google.maps.MapTypeId.TERRAIN
    // ROADMAP
    };
    var mapContainer = $(elem).get(0);
    this.map = new google.maps.Map(mapContainer, mapOptions);
    if (this.supportsFullScreen(mapContainer)) {
      var controlDiv = document.createElement('div');
      this.addGoogleMapsControls(controlDiv, this.map);
      controlDiv.index = 1;
      this.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(controlDiv);
    }
    this.map.addListener('bounds_changed', function() {
      var bounds = caller.map.getBounds();
      var load = false;
      if (!caller.bounds) {
        caller.bounds = bounds;
        load = true;
      } else {
        var d = google.maps.geometry.spherical.computeDistanceBetween(caller.bounds.getNorthEast(), caller.bounds.getSouthWest());
        var d1 = google.maps.geometry.spherical.computeDistanceBetween(caller.bounds.getNorthEast(), bounds.getNorthEast());
        var d2 = google.maps.geometry.spherical.computeDistanceBetween(caller.bounds.getSouthWest(), bounds.getSouthWest());
        // new corners have changed by more than 10%
        if (d1 > d / 10  || d2 > d / 10) {
          caller.bounds = bounds;
          caller.center = caller.map.getCenter();
          load = true;
        }
      }
      if (load) {
        caller.showTracksForMap(caller.data, bounds);
      }
    });
  } else {
    var boundsChanges = true;
    if (caller.center) {
      boundsChanges = !(this.map.getCenter().equals(this.center));
      this.map.setCenter(caller.center);
      if (this.map.getZoom() != 8 && boundsChanges) {
        this.map.setZoom(8);
      }
    }
    if (!boundsChanges) {
      this.showTracksForMap(this.data, this.map.getBounds());
    }
  }
  $(elem).css("visibility", "visible");
}

Client.prototype.showTracksForMap = function(data, bounds) {
  var caller = this;
  this.loadTracksForMap(data, bounds);
  if (!this.trackList) {
    return;
  }
  var tracks = this.trackList.track;
  if (this.markers) {
    for (var i = 0; i < this.markers.length; i++) {
      this.markers[i].setMap(null);
    }
  }
  this.markers = [];
  for (var track = 0; track < tracks.length; track++) {
    var mlat = tracks[track].latitude;
    var mlon = tracks[track].longitude;
    var p = null;
    if (mlat && mlon) {
      p = new google.maps.LatLng(mlat, mlon);
    }
    var trackDataList = tracks[track]['trackdata'];
    if (trackDataList && trackDataList.length > 0) {
      var td = trackDataList[0];
      if (td.highestPoint) {
        p = new google.maps.LatLng(td.highestPoint.lat, td.highestPoint.lng);
      } else if (td.startPoint) {
        p = new google.maps.LatLng(td.startPoint.lat, td.startPoint.lng);
      }
    }
    if (p) {
      var marker = new google.maps.Marker({
        position : p,
        map : this.map,
        title : tracks[track].name,
        track : tracks[track]
      });
      marker.addListener('click', function() {
        var track = this.track;
        var content = '<div class="marker">';
        if (track.date) {
          content += '<div id="siteNotice">' + $.format.date(parseDate(track.date), 'dd MMM yyyy') + '</div>';
        }
        content += '<h3 id="firstHeading">';
        content += '<a href="../pages/detail.html#' + encodeURIComponent(track.name) + '">' + track.name + '</a>';
        content += '</h3>';
        if (track.description) {
          content += '<div id="bodyContent"><p>' + track.description + '</p></div>';
        }
        content += '</div>';
        var infowindow = new google.maps.InfoWindow({
          content: content
        });
        if (caller.infowindow) {
          caller.infowindow.close();
        }
        infowindow.open(caller.map, this);
        caller.infowindow = infowindow;
      });
      this.markers.push(marker);
    }
  }
  this.addImagesToMap('#image-parent', tracks);
}

Client.prototype.addImagesToMap = function(elem, tracks) {
  if (this.initCarousel) {
    $(elem).empty().mustache('image-list', $.i18n.map);
  }
  var imageCount = 0;
  for (var track = 0; track < tracks.length; track++) {
    var images = tracks[track]['image'];
    if (images && images.length > 0) {
      var img = images[0];
      if (img != null) {
        this.showImage('#image-list', tracks[track].name, img, i, "SMALL");
        imageCount++;
      }
    }
  }
  if (imageCount > 0) {
    var maxVisible = Math.floor($('#main').outerWidth() / 160);
    $(elem).css('display', 'inherit');
    $('#image-list').slick({
      infinite: (imageCount >= maxVisible),
      slidesToShow: 1,
      variableWidth: true,
      swipeToSlide: true,
      centerMode: false,
      draggable: false,
      dots: (imageCount >= maxVisible),
      arrows: (imageCount >= maxVisible)
    });
    $('#image-list').Chocolat({
      loop: true,
      imageSize: 'contain'
    });
    this.initCarousel = true;
  } else {
    this.initCarousel = false;
    $(elem).css('display', 'none').empty().mustache('image-list', $.i18n.map);
  }
}

Client.prototype.loadTracksForMap = function(data, bounds) {
  var caller = this;
  data.bounds = {
      north: bounds.getNorthEast().lat(),
      east: bounds.getNorthEast().lng(),
      south: bounds.getSouthWest().lat(),
      west: bounds.getSouthWest().lng()
  };
  caller.trackList = caller.postData(baseurl + serviceSearchUrl, "POST", data);
  var suffix = "";
  suffix = addQueryParam(suffix, "name", data.search);
  suffix = addQueryParam(suffix, "activity", data.activity);
  if (history.pushState) {
    history.pushState(data.search, title_bar, "map.html" + suffix);
  }
}
