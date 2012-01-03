org-html-slides
========================================

You have an outline written in Emacs org-mode.  Add some JavaScript
and CSS from this project, and you have an interactive
slide presentation!

Supports anything org-mode can export: bulleted lists, code blocks,
images, etc.

Should work in most modern web browsers; developed for Google Chrome.

Org-html-slides is written in [ClojureScript](https://github.com/clojure/clojurescript).


Using in Your Org-mode Files
========================================

**Step 1.** Copy the following files from `out/production/` to the
directory containing your .org file:

    org-html-slides.js

    common.css
    goog-common.css
    projection.css
    screen.css

**Step 2.** Add the following lines to the bottom of your .org file:

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

**Step 3.** Close and re-open your .org file. Type `y` to accept the
buffer-local variables.

**Step 4.** For each org-mode headline that you want to make into a
slide, add the `:slide:` tag by typing `C-c C-c s RET` with the cursor
on the headline.

**Step 5.** Type `C-c C-e h` in your .org file to export as HTML.

Repeat Step 5 whenever you modify the .org file.


Playing the Slide Show
----------------------

Open the generated HTML file in your browser and type `t` to begin the
slide show.

The Space, Enter, Page Down, and `n` keys advance to the next slide.

The Page Up and `p` keys go back to the previous slide.

The `t` key toggles between slide-show and normal views.


Changing Styles
--------------------

You can modify the appearance of your slides by editing the stylesheets:

* `projection.css` affects only the slide-show view
* `screen.css` affects only the normal view
* `common.css` affects both


Development Bootstrap
========================================

To develop and build org-html-slides, you will need the following
programs already installed:

* Bash
* Curl
* Git
* Java Development Kit

Run `script/bootstrap` to download additional build dependencies.


Development Examples
========================================

(Depends on "Development Bootstrap")

Run `script/build development` to generate JavaScript files for the examples.

You will need [Emacs](http://www.gnu.org/software/emacs/) (version 23+ recommended) and
[org-mode](http://orgmode.org/) (version 7+ recommended) to generate the HTML.

Open `examples/example-development.org` in Emacs and type `C-c C-e b`.
Emacs will generate an HTML file and open it in your default
browser. Type `t` to begin the slide show.


Rebuilding Production Files
========================================

(Depends on "Development Bootstrap")

Run `script/build production` to rebuild standalone JavaScript and CSS
files in `out/production/`


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
