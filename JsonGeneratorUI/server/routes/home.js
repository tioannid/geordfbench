var express = require("express");
var router = express.Router();

/* GET home page. */
router.get("/", function (req, res, next) {
  res.send(
    "Endpoint API for GeoRDFBench Framework JSON Benchmarking Specifications"
  );
});

module.exports = router;
