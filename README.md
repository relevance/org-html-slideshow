org-html-slides
========================================

You have an outline written in Emacs org-mode.  Add some JavaScript
and CSS from org-html-slides, and you have an interactive
presentation. Supports bulleted lists, code blocks, and images.


Building
========================================

Org-html-slides is written in ClojureScript. You will need the
following installed to run the build process:

* Bash
* Curl
* Java Development Kit

Run `script/bootstrap` to download dependencies.

Then run `script/build` to build `out/org-html-slides.js`.
