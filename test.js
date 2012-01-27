// # Testing with qcjs
//
// To test this project ensure that [phantomjs] is installed
// and `quickcheck/qc.js` is present, then compile the
// clojurescript to a single file `out/all.js`. If advanced
// optimization is used, make sure `externs/qc.js` is also
// used. Run the tests with `phantomjs <this-file>.js`.
//
// This file originally came from the [qcjs] project.
//
// [phantomjs]: http://phantomjs.org
// [qcjs]: https://github.com/jedahu/qcjs

phantom.injectJs('quickcheck/qc.js');
phantom.injectJs('out/all.js');

Object.prototype.toString = function() {
  return cljs.core.pr_str(this);
};

var listener = new qc.ConsoleListener;
listener.failCount = 0;
listener.done = function() {
  phantom.exit(this.failCount);
};
listener.failure = function(result) {
  listener.failCount++;
  qc.ConsoleListener.prototype.failure.call(this, result);
};
try {
  qc.runProps(new qc.Config({}), listener);
} catch (e) {
  console.log(e);
  phantom.exit(1);
}
