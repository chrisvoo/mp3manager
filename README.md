# MP3 Manager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df633fac03554e5a98540824373cd3a8)](https://www.codacy.com/app/chrisvoo/mp3manager?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=chrisvoo/mp3manager&amp;utm_campaign=Badge_Grade)
<!-- [![Build Status](https://travis-ci.com/chrisvoo/mp3manager.svg?branch=master)](https://travis-ci.com/chrisvoo/mp3manager) -->

- [Requirements](#requirements)
  - [Windows notes](#windows-notes)
- [Installation](#installation)
- [Description and usage](#description-and-usage)
  - [Scanner](#scanner)

This project is intended to manage a large collection of MP3 files both from the browser and from a React Native app. The browser version may do some privileged tasks like user management, absent from the mobile version which will focus more on listening to music.
The interfaces should allow the user:

- to listen to music, streamed by a server in the same LAN as the client (Web and mobile client)
- to manage music files (delete them and edit their metatags)
- display lyrics

Extenal webservices API may be used, such as [MusicBrainz](https://musicbrainz.org/) or [Discogs](https://www.discogs.com/). There are a couple of classes in the code which deals with their API, however due to the great amount of results they give, I've not found a way to use them to automatically edit music file. Probably this part could be done inside the browser, allowing the user to choose the appropriate result.

## Requirements

- __Node.js >= 12.*__: this is the LTS version I've used, but it should work with every version superior to 7.6, which supports `async/await` out of the box without requiring transpilation.
- __Java 8__: the scanner is a multithreaded Java app
