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


Examples
========================================

Run `script/build development` to generate JavaScript files.

You will need [Emacs](http://www.gnu.org/software/emacs/) and
[org-mode](http://orgmode.org/) to generate the HTML.

Open `examples/example-development.org` in Emacs and type `C-c C-e b`.
Emacs will generate an HTML file and open it in your default browser.


Using in Your Org Files
========================================

Run `script/build production` to generate a standalone JavaScript file.

Copy the following files from `out/` to the directory containing your .org file:

    org-html-slides.js
    goog-common.css
    common.css
    screen.css
    projection.css

Add the following lines to the bottom of your .org file:

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

Type `C-c C-e h` in your .org file to export as HTML.

You can modify the appearance of your slides by editing the stylesheets:

* `projection.css` affects only the slide-show view
* `screen.css` affects only the normal view
* `common.css` affects both
