# Music Manager

<!-- [![Build Status](https://travis-ci.com/chrisvoo/mp3manager.svg?branch=master)](https://travis-ci.com/chrisvoo/mp3manager) -->

This project is intended to manage a large collection of MP3 files from the browser.

## Requirements

- __Node.js >= 12.*__: this is the LTS version I've used, but it should work with every version superior to 7.6, which supports `async/await` out of the box without requiring transpilation.
  - some global node modules like [shx](https://github.com/shelljs/shx), [PM2](https://pm2.keymetrics.io/) and [TypeScript](https://github.com/microsoft/TypeScript/)
- __Java 8__: the scanner is a multithreaded Java app
