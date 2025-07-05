var express = require("express");
var router = express.Router();
var os = require("os");

// get a list of .dotenv configuration
router.get("/", (req, res) => {
  let endpointConfig = {
    PORT: process.env.PORT,
    JSON_LIB_PATH: process.env.JSON_LIB_PATH,
    HOSTNAME: os.hostname(),
    PLATFORM: os.version().concat(" v", os.release(), ", ", os.machine()),
  };
  return res.json(endpointConfig);
});

module.exports = router;
