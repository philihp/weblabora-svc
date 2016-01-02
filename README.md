# Weblabora as a Service

This was thrown together late one September night while I was fed up with Play Framework growing increasingly outdated and wanted to
switch over to Ruby or perhaps something entirely in Javascript.

It was made from some Heroku boilerplate, and should be able to be deployed relatively easily.

## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku Toolbelt](https://toolbelt.heroku.com/).

```sh

$ git clone https://github.com/philihp/weblabora-svc.git
$ cd weblabora-svc
$ mvn compile dependency:copy-dependencies
$ scripts/run

Your app should now be running on [localhost:5000](http://localhost:5000/).

You should now be able to PUT to `/`, with a raw text of all of the move lists of a game. The server will then redirect you to a GET address which will
contain a JSON CLOB of the board. Fancypants caching involved too, although it's probably overkill.

## Deploying to Heroku

```sh
$ heroku create
$ git push heroku master
$ heroku open
```


