# Org-HTML-Slideshow

You have an outline written in Emacs Org-Mode.  Export it to HTML. Add
Org-HTML-Slideshow, and you have an interactive slide presentation
that runs in a web browser!

Supports anything Org-Mode can export: bulleted lists, code blocks,
images, etc.

Should work in most modern web browsers; tested only in Google Chrome
29.0.1547.76.

Org-HTML-Slideshow is written in
[ClojureScript](https://github.com/clojure/clojurescript).



## Recommended Emacs Platform

* [GNU Emacs](http://www.gnu.org/software/emacs/) version 24+
  * Check version with `M-x emacs-version`
* [Org-Mode](http://orgmode.org/) version 8+
  * Check version by opening an .org file and `M-x org-version`
* [Htmlize](http://www.emacswiki.org/emacs/Htmlize) version 1.37+



## Using in Your Org-mode Files

**Step 1.** Copy the following files from the `production` directory
to the directory containing your .org file:

    org-html-slideshow.js
    common.css
    presenter.css
    projection.css
    screen.css

**Step 2.** Add the following lines to the **bottom** of your .org file:

    #+OPTIONS: num:nil tags:t

    #+TAGS: slide(s)

    #+HTML_HEAD_EXTRA: <link rel="stylesheet" type="text/css" href="common.css" />
    #+HTML_HEAD_EXTRA: <link rel="stylesheet" type="text/css" href="screen.css" media="screen" />
    #+HTML_HEAD_EXTRA: <link rel="stylesheet" type="text/css" href="projection.css" media="projection" />
    #+HTML_HEAD_EXTRA: <link rel="stylesheet" type="text/css" href="presenter.css" media="presenter" />

    #+BEGIN_HTML
    <script type="text/javascript" src="org-html-slideshow.js"></script>
    #+END_HTML

    # Local Variables:
    # org-html-head-include-default-style: nil
    # org-html-head-include-scripts: nil
    # End:

**Step 3.** Close and re-open your .org file. Type `y` to accept the
buffer-local variables.


### Adding Tags to Your Org-Mode File

For each org-mode headline that you want to make into a slide, add the
`:slide:` tag by typing `C-c C-c s RET` with the cursor on the
headline.

Additional tags will be added as CSS classes on the slides.

Read more about [tags](http://orgmode.org/manual/Tags.html)
in the Org-Mode manual.

See the files `example.org` and `example.html` for more examples of
things you can do with Org-Mode.


### Exporting to HTML

Type `C-c C-e h h` in your .org file to export as HTML. Repeat whenever
you modify the .org file.

Read more about [HTML export](http://orgmode.org/manual/HTML-export.html)
in the Org-Mode manual.


### Playing the Slide Show

Open the generated HTML file in your browser and type `t` to begin the
slide show.

The Space, Enter, Page Down, and `n` keys advance to the next slide.

The Page Up and `p` keys go back to the previous slide.

The `t` key toggles between slide-show and normal views.

Move the mouse to the lower right-hand corner of the screen to show a
control panel with links to navigate the slide show by mouse.


### Presenter Preview

While playing the slide show, click on the control panel's "Open
presenter preview" link. This will open a new window showing the
currently visible slide, upcoming slide, wall clock time, and elapsed
time since the presentation began.


### Presenter Notes

Add a sub-heading with the tag `:notes:` beneath a `:slide:`
heading. The content under the notes heading will be displayed in the
Presenter Preview window with the slide.


### Changing Styles

You can modify the appearance of your slides by editing the stylesheets:

* `projection.css` affects only the slide-show view
* `screen.css` affects only the normal view
* `common.css` affects both
* `presenter.css` affects only the presenter preview



## Development

To develop and build org-html-slides, you will need the following
programs already installed:

* [Git][git]
* [Leiningen][lein] 2.0.0 or later
* [Java Development Kit][jdk] (JDK) 1.6 or later

[git]: http://git-scm.com/
[lein]: https://github.com/technomancy/leiningen
[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html


In the top-level directory of this project, run the following commands
to download additional dependencies:

    git submodule init
    git submodule update


### Rebuilding Development Examples

Build the development version (one file, unoptimized, readable
JavaScript source) with:

    lein cljsbuild once development

The JavaScript file will be written to `out/development/org-html-slideshow.js`.

You will need [Emacs](http://www.gnu.org/software/emacs/) (version 24+ recommended) and
[org-mode](http://orgmode.org/) (version 8+ recommended) to generate the HTML.

Open `example.org` in Emacs and type `C-c C-e b`.  Emacs will generate
an HTML file and open it in your default browser. Type `t` to begin
the slide show.


### Rebuilding Production Files

Build the production version (one file, optimized, not human-readable)
with:

    lein cljsbuild once production

The JavaScript file will be written to `production/org-html-slideshow.js`.


## Change Log

* **27-Sept-2013:** Switch to Emacs 24 and Org-mode 8

  Org-mode 8 introduced breaking changes in the way it exports HTML,
  necessitating breaking changes in org-html-slideshow.

  For Emacs 23 and Org-mode 7, use a version of org-html-slideshow on
  the Git branch [emacs23-org7](https://github.com/relevance/org-html-slideshow/tree/emacs23-org7)

* **11-Dec-2011:** Initial release


## Copyright & Contributions

There is no copyright. I dedicate this work to the public domain. 

I am not actively developing new features for this project.


### Contributors

* Stuart Sierra (primary author)
* Craig Andera
* Alex Redington
