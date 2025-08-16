var express = require("express");
var router = express.Router();
const packageJson = require("../package.json");

/* GET home page. */
router.get("/", function (req, res, next) {
  res.send(
    `${packageJson.description} v${packageJson.version} is up and running at port ${req.app.locals.endpointConfig.PORT}!`
  );
});

module.exports = router;
