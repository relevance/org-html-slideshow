org-html-slides
========================================

You have an outline written in Emacs org-mode.  Add some JavaScript
and CSS from this project, and you have an interactive
slide presentation!

Supports anything org-mode can export: bulleted lists, code blocks,
images, etc.

Should work in most modern web browsers; developed for Google Chrome.

Org-html-slides is written in [ClojureScript](https://github.com/clojure/clojurescript).


Bootstrap
========================================

You will need the following programs already installed to run the
bootstrap process:

* Bash
* Curl
* Git
* Java Development Kit

Run `script/bootstrap` to download additional build dependencies.


Examples
========================================

Run `script/build development` to generate JavaScript files for the examples.

You will need [Emacs](http://www.gnu.org/software/emacs/) (version 23+ recommended) and
[org-mode](http://orgmode.org/) (version 7+ recommended) to generate the HTML.

Open `examples/example-development.org` in Emacs and type `C-c C-e b`.
Emacs will generate an HTML file and open it in your default
browser. Type `t` to begin the slide show.


Using in Your Org-mode Files
========================================

Run `script/build production` to generate a standalone JavaScript file.

Copy the following files from `out/` to the directory containing your .org file:

    org-html-slides.js
    goog-common.css
    common.css
    screen.css
    projection.css

Add the following lines to the bottom of your .org file:

    #+TAGS: slide(s)

    #+STYLE: <link rel="stylesheet" type="text/css" href="goog-common.css" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="common.css" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="screen.css" media="screen" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="projection.css" media="projection" />

    #+BEGIN_HTML
    <script type="text/javascript" src="org-html-slides.js"></script>
    #+END_HTML

    # Local Variables:
    # org-export-html-style-include-default: nil
    # org-export-html-style-include-scripts: nil
    # End:

Close and re-open your .org file. Type `y` to accept the buffer-local
variables. 

For each org-mode headline that you want to make into a slide, add the
`:slide:` tag by typing `C-c C-c s RET` with the cursor on the
headline.

Type `C-c C-e h` in your .org file to export as HTML. Open the HTML
file in your browser and type `t` to begin the slide show.

You can modify the appearance of your slides by editing the stylesheets:

* `projection.css` affects only the slide-show view
* `screen.css` affects only the normal view
* `common.css` affects both


TODO
========================================

* Better stylesheets
* Home/End keys to jump to first/last slide
* ? key to display on-screen help
* Mouse navigation: click-to-advance, on-screen controls
* Link visible in original document to begin slide show
* "Slide X of N" display
* Jump to slide from a list
* Slide transitions
* Animation?


Copyright and License
========================================

Contains code from the
[Google Closure](http://code.google.com/closure/) project,
with the following copyright:

    Copyright 2009 The Closure Library Authors. All Rights Reserved.
    Use of this source code is governed by the Apache License, Version 2.0.

