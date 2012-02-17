Org-HTML-Slideshow
========================================

You have an outline written in Emacs Org-Mode.  Export it to HTML. Add
Org-HTML-Slideshow, and you have an interactive slide presentation
that runs in a web browser!

Supports anything Org-Mode can export: bulleted lists, code blocks,
images, etc.

Should work in most modern web browsers; tested only in Google Chrome
18.0.1025.11 beta.

Org-HTML-Slideshow is written in
[ClojureScript](https://github.com/clojure/clojurescript).


Using in Your Org-mode Files
========================================

**Step 1.** Copy the following files from the `production` directory
to the directory containing your .org file:

    org-html-slideshow.js
    common.css
    presenter.css
    projection.css
    screen.css

**Step 2.** Add the following lines to the **bottom** of your .org file:

    #+TAGS: slide(s)

    #+STYLE: <link rel="stylesheet" type="text/css" href="common.css" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="screen.css" media="screen" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="projection.css" media="projection" />
    #+STYLE: <link rel="stylesheet" type="text/css" href="presenter.css" media="presenter" />

    #+BEGIN_HTML
    <script type="text/javascript" src="org-html-slideshow.js"></script>
    #+END_HTML

    # Local Variables:
    # org-export-html-style-include-default: nil
    # org-export-html-style-include-scripts: nil
    # End:

**Step 3.** Close and re-open your .org file. Type `y` to accept the
buffer-local variables.


Adding Tags to Your Org-Mode File
---------------------------------

For each org-mode headline that you want to make into a slide, add the
`:slide:` tag by typing `C-c C-c s RET` with the cursor on the
headline.

Additional tags will be added as CSS classes on the slides.

Read more about [tags in the Org-Mode manual](http://orgmode.org/manual/Tags.html).


Exporting to HTML
-----------------

Type `C-c C-e h` in your .org file to export as HTML. Repeat whenever
you modify the .org file.

Read more about [HTML export in the Org-Mode manual](http://orgmode.org/manual/HTML-export.html).


Playing the Slide Show
----------------------

Open the generated HTML file in your browser and type `t` to begin the
slide show.

The Space, Enter, Page Down, and `n` keys advance to the next slide.

The Page Up and `p` keys go back to the previous slide.

The `t` key toggles between slide-show and normal views.

Move the mouse to the lower right-hand corner of the screen to show a
control panel with links to navigate the slide show by mouse.


Presenter Preview
--------------------

While playing the slide show, click on the control panel's "Open
presenter preview" link. This will open a new window showing the
currently visible slide, upcoming slide, wall clock time, and elapsed
time since the presentation began.

Even with the presenter preview window open, the main slide show
window **must** be focused (the "active" window) in order for the
keyboard commands to work.


Changing Styles
--------------------

You can modify the appearance of your slides by editing the stylesheets:

* `projection.css` affects only the slide-show view
* `screen.css` affects only the normal view
* `common.css` affects both
* `presenter.css` affects only the presenter preview


Development
========================================

To develop and build org-html-slides, you will need the following
programs already installed:

* [Git][git]
* [Leiningen][lein] 1.6.2 or later
* [Java Development Kit][jdk] (JDK) 1.6 or later

[git]: http://git-scm.com/
[lein]: https://github.com/technomancy/leiningen
[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html


In the top-level directory of this project, run the following commands
to download additional dependencies:

    git submodule update
    lein deps


Rebuilding Development Examples
-------------------------------

Launch a Clojure REPL with:

    lein repl

Then run the build with:

    (use 'build)
    (build :development)

You will need [Emacs](http://www.gnu.org/software/emacs/) (version 23+ recommended) and
[org-mode](http://orgmode.org/) (version 7+ recommended) to generate the HTML.

Open `example.org` in Emacs and type `C-c C-e b`.  Emacs will generate
an HTML file and open it in your default browser. Type `t` to begin
the slide show.


Rebuilding Production Files
---------------------------

Launch a Clojure REPL with:

    lein repl

Then run the build with:

    (use 'build)
    (build :production)



TODO
========================================

* Better stylesheets
* ? key to display on-screen help
* click mouse to advance slides
* Link visible in original document to begin slide show
* "Slide X of N" display
* Jump to slide from a list
* Slide transitions?
* Animation?



Copyright & Contributions
========================================

There is no copyright. I dedicate this work to the public domain. If
you want me to use your fixes/improvements, you must do the same.
