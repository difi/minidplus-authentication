(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';

/*
* Step indicator is updated
*/

var toggleForm = function toggleForm() {
  var $ = jQuery.noConflict();

  var $firstProgress = $('.js-makeProgress-1');
  var $secondProgress = $('.js-makeProgress-2');
  var progressDots = $('.fm-Progress_Dot');

    // Advance progress
    if($firstProgress.length == 0) {
      $(progressDots[0]).removeClass('active');
      $(progressDots[1]).addClass('active');
    }

};

toggleForm();

},{}]},{},[1]);
