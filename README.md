# MP3 Manager

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/df633fac03554e5a98540824373cd3a8)](https://www.codacy.com/app/chrisvoo/mp3manager?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=chrisvoo/mp3manager&amp;utm_campaign=Badge_Grade)
<!-- [![Build Status](https://travis-ci.com/chrisvoo/mp3manager.svg?branch=master)](https://travis-ci.com/chrisvoo/mp3manager) -->

- [Requirements](#requirements)
  - [Windows notes](#windows-notes)
- [Installation](#installation)
- [Description and usage](#description-and-usage)
  - [Scanner](#scanner)

This project is intended to manage a large collection of MP3 files both from the browser and from a React Native app. The browser version should do some privileged tasks like user management, absent from the mobile version which will focus more on listening to music.
The interfaces should allow the user:

- to listen to music, streamed by a server in the same LAN as the client (Web and mobile client)
- to manage music files (delete them and edit their metatags)
- display lyrics

Extenal webservices API may be used, such as [MusicBrainz](https://musicbrainz.org/) or [Discogs](https://www.discogs.com/). There are a couple of classes in the code which deals with their API, however due to the great amount of results they give, I've not found a way to use them to automatically edit music file. Probably this part could be done inside the browser, allowing the user to choose the appropriate result.

## Requirements

- __pnpm__: [package manager](https://pnpm.js.org/en/) to easily manage a monorepo.
- __Node.js >= 10.*__: this is the LTS version I've used, but it should work with every version superior to 7.6, which supports `async/await` out of the box without requiring transpilation.
- __ffmpeg__: used to read MP3 files' metadata. [Windows builds](https://ffmpeg.zeranoe.com/builds/) or `sudo apt install ffmpeg` for Debian like distros.
- __mediainfo__: used to get the files' audio details (bitrate, duration). You can get it [here](https://mediaarea.net/en/MediaInfo/Download)

### Windows notes

It's recommended to install [Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/faq). Once [installed](https://docs.microsoft.com/en-us/windows/wsl/install-win10#install-the-windows-subsystem-for-linux), you can choose a Linux distribution to be run like an app and install Redis there. Windows will be able to communicate with it ([see also this post of RedisLabs](https://redislabs.com/blog/redis-on-windows-10/)). After that, you can just run whatever Redis client you want, for example [redis-commander](https://github.com/joeferner/redis-commander).

Regarding GCC, you can install a current version of [MinGW-w64](https://sourceforge.net/projects/mingw-w64/), specify win32 as thread when requested and additionally, choose the architecture x86_64. Put the `bin` directory in your `PATH` as last step.

## Installation

This is a monorepo which hosts all the parts of the architecture. You can manage Node.js dependencies with [Lerna](https://lernajs.io/). To start modifying this project, just do the following after having cloned this repo:

```bash
npm install --global pnpm cross-env
pnpm recursive install
```

## Test

Run the following commands in the root of the project:

- `mm install -u root -p mypass`: it creates the database test
- `npm test`

## Description and usage

This project is composed by two main parts at the moment:

- API server: this endpoint provides the frontend with a set of GraphQL queries and mutations which can be executed by the frontend.
- Streaming server: this is responsible for serving your music collection files

To start these two components, just type the following commands:

```bash
npm install --global pm2
pm2 start <ROOT_PROJECT_DIR>/scripts/ecosystem.config.js
```

This will launch all the services through [PM2](https://pm2.io/doc/en/runtime/overview/) and will allow you to view the standard output/error with `pm2 log <package_name>` command.

### Scanner

This service is responsible for scanning a list of paths, seaching recursively for all the MP3 files it can find. To do that, it makes use of [Bull](https://github.com/OptimalBits/bull), a queue manager which runs automatically a job for every directory it finds in the trees of the specified paths. The more CPUs your machine has, the faster it is, because every job is a spawned process and jobs' references are stored inside Redis.

The database structure doesn't allow duplicates. The uniqueness is just given by the MD5 calculated for every file without considering its metadata (ID3). The MD5 is stored inside the ID3 Comment tag. This is the algorithm with pseudocode:

```text
  for every file "f":
    - read comment metatag "c"
    if c has MD5 hash:
        if MD5(f) === c:
            - continue;
        else:
            - store MD5(f) in comment tag
            - read metadata "m"
            - UPDATE m inside the database
    else:
        - INSERT f inside the DB
        - UPSERT m inside the DB
        if the UNIQUE constraint fails:
            - INSERT f inside the table file_duplicates
```

In order to calculate the MD5 just on the data without considering the metadata, I use a C program called [mp3hash](https://github.com/sptim/mp3hash). This program is imported as a GIT submodule in this repo under the directory `external/mp3hash`. Do the following to obtain the executable:

```bash
git submodule init
git submodule update --remote
cd external/mp3hash
gcc -o mp3hash mp3hash.c md5/md5.c
```

If everything worked fine, you should see a `mp3hash` executable.
