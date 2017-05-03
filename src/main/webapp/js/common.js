function log(msg) {
  try {
    console.log(msg);
  } catch (err) {
  }
  try {
    System.Debug.outputString(err);
  } catch (err) {
  }
}

function setValue(id, value) {
  var elem = document.getElementById(id);
  if (elem) {
    elem.value = value ? value : '';
  }
}

function getValue(id, value) {
  value = document.getElementById(id).value;
  if (value == '') {
    return null;
  }
  return value;
}

function setText(id, value, skipIfEmpty) {
  if (value) {
    $(id).text(value);
  } else {
    $(id).text(skipIfEmpty ? '' : '-');
  }
}

function setFloat(id, value) {
  if (value) {
    $(id).text(value.toFixed(2));
  } else {
    $(id).text('-');
  }
}

function setDate(id, value) {
  if (value) {
    $(id).text($.format.date(parseDate(value), 'dd MMM yyyy'));
  } else {
    $(id).text('-');
  }
}

function setChecked(id, value) {
  var elem = document.getElementById(id);
  if (elem)
    elem.checked = value;
}

function getChecked(id, value) {
  return document.getElementById(id).checked;
}

function parseDate(value) {
  if (value == null) {
    return null;
  }
  return new Date(value);
}

function printDate(value) {
  if (value == null) {
    return null;
  }
  var date = value.getDate();
  date = (date < 10) ? '0' + date : '' + date;
  var month = value.getMonth() + 1;
  month = (month < 10) ? '0' + month : '' + month;
  return value.getFullYear() + '-' + month + '-' + date;
}

function addQueryParam(params, name, value) {
  if (value) {
    if (params.length > 0) {
      params += "&";
    } else {
      params = "?";
    }
    params += name + "=" + encodeURIComponent(value)
  }
  return params;
}

/**
 * Copyright (c) 2010 Conrad Irwin <conrad@rapportive.com> MIT license. Based
 * loosely on original: Copyright (c) 2008 mkmanning MIT license.
 * 
 * Parses CGI query strings into javascript objects.
 * 
 * See the README for details.
 */
(function($) {
  $.parseQuery = function(options) {

    var config = {
      query : window.location.search || ""
    }, params = {};

    if (typeof options === 'string') {
      options = {
        query : options
      };
    }
    $.extend(config, $.parseQuery, options);
    config.query = config.query.replace(/^\?/, '');

    if (config.query.length > 0) {
      $.each(config.query.split(config.separator), function(i, param) {
        var pair = param.split('='), key = config.decode(pair.shift(), null).toString(), value = config.decode(
            pair.length ? pair.join('=') : null, key);

        if (config.array_keys.test ? config.array_keys.test(key) : config.array_keys(key)) {
          params[key] = params[key] || [];
          params[key].push(value);
        } else {
          params[key] = value;
        }
      });
    }
    return params;
  };
  $.parseQuery.decode = $.parseQuery.default_decode = function(string) {
    return decodeURIComponent((string || "").replace(/\+/g, ' '));
  };
  $.parseQuery.array_keys = function() {
    return false;
  };
  $.parseQuery.separator = "&";
}(window.jQuery || window.Zepto));

/*
 * ! jQuery Cookie Plugin https://github.com/carhartl/jquery-cookie
 * 
 * Copyright 2011, Klaus Hartl Dual licensed under the MIT or GPL Version 2
 * licenses. http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/GPL-2.0
 */
jQuery.cookie = function(key, value, options) {
  // key and at least value given, set cookie...
  if (arguments.length > 1
      && (!/Object/.test(Object.prototype.toString.call(value)) || value === null || value === undefined)) {
    options = $.extend({}, options);

    if (value === null || value === undefined) {
      options.expires = -1;
    }

    if (typeof options.expires === 'number') {
      var days = options.expires, t = options.expires = new Date();
      t.setDate(t.getDate() + days);
    }

    value = String(value);

    return (document.cookie = [ encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
    // use expires attribute, max-age is not supported by IE
    options.expires ? '; expires=' + options.expires.toUTCString() : '', options.path ? '; path=' + options.path : '',
        options.domain ? '; domain=' + options.domain : '', options.secure ? '; secure' : '' ].join(''));
  }

  // key and possibly options given, get cookie...
  options = value || {};
  var decode = options.raw ? function(s) {
    return s;
  } : decodeURIComponent;

  var pairs = document.cookie.split('; ');
  for (var i = 0, pair; pair = pairs[i] && pairs[i].split('='); i++) {
    if (decode(pair[0]) === key) {
      // IE saves cookies with empty string as "c; ", e.g. without "=" as
      // opposed to EOMB,
      // thus pair[1] may be undefined
      return decode(pair[1] || '');
    }
  }
  return null;
};
