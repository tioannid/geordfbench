var express = require("express");
var router = express.Router();
var os = require("os");

// get a list of persisted host configuration
// from the .env file
router.get("/", (req, res) => {
  // we have to read directly for the .env file
  // since the req.app.locals.endpointConfig values
  // could have been altered!
  let endpointConfig = {
    PORT: process.env.PORT,
    JSON_LIB_PATH: process.env.JSON_LIB_PATH,
    HOSTNAME: os.hostname(),
    PLATFORM: os.version().concat(" v", os.release(), ", ", os.machine()),
    CRUD_MAP: JSON.parse(process.env.CRUD_MAP),
  };
  return res.json(endpointConfig);
});

// get a list of .dotenv configuration
router.put("/", (req, res) => {
  // TODO: remove the following debugging/testing line
  res.app.locals.endpointConfig.CRUD_MAP["executionspecs"] = "CRUD";
  return res.status(200).json(req.app.locals.endpointConfig);
});

// get a list of dynamic/current host configuration
// as it stored in the locals.endpointConfig variable
router.get("/current", (req, res) => {
  return res.json(req.app.locals.endpointConfig);
});

module.exports = router;
