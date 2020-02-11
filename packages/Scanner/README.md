# Scanner

The basic idea is to walk recursively a directory searching for MP3 files and insert them into
MongoDB. At the beginning the same thing was performed by Node.js, but now I want to explore what
I can do using Java high level concurrency API, more specifically with the [ForkJoin framework](https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html).

## Resources

The music files are freely downloadable from [Open Music Archive](http://www.openmusicarchive.org/) (Creative Commons
licences and public domain).