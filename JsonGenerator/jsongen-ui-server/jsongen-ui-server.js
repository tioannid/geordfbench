const express = require("express");
const cors = require("cors");
const path = require("path");
const packageJson = require("./package.json");
const logger = require("morgan");
const fs = require("fs");
const YAML = require("yaml");
const os = require("os");

// run dotenv
require("dotenv").config();

// get the ACCESS_TOKEN_SECRET
global.ACCESS_TOKEN_SECRET = process.env.ACCESS_TOKEN_SECRET;

// get the REFRESH_TOKEN_SECRET
global.REFRESH_TOKEN_SECRET = process.env.REFRESH_TOKEN_SECRET;

const app = express();

// calculate Host, Port and Url for Accessing the Endpoint
const ACCESS_ENDPOINT_HOST =
  process.env.ACCESS_ENDPOINT_ENV === "DEV"
    ? process.env.DEV_ACCESS_ENDPOINT_HOST
    : process.env.PROD_ACCESS_ENDPOINT_HOST;
const ACCESS_ENDPOINT_PORT =
  process.env.ACCESS_ENDPOINT_ENV === "DEV"
    ? process.env.DEV_ACCESS_ENDPOINT_PORT
    : process.env.PROD_ACCESS_ENDPOINT_PORT;
var ACCESS_ENDPOINT_URL = `https://${ACCESS_ENDPOINT_HOST}`;
ACCESS_ENDPOINT_URL = ACCESS_ENDPOINT_PORT
  ? `${ACCESS_ENDPOINT_URL}:${ACCESS_ENDPOINT_PORT}`
  : `${ACCESS_ENDPOINT_URL}`;
// calculate Host, Port and Url for Accessing the UI
const ACCESS_UI_HOST =
  process.env.ACCESS_UI_ENV === "DEV"
    ? process.env.DEV_ACCESS_UI_HOST
    : process.env.PROD_ACCESS_UI_HOST;
const ACCESS_UI_PORT =
  process.env.ACCESS_UI_ENV === "DEV"
    ? process.env.DEV_ACCESS_UI_PORT
    : process.env.PROD_ACCESS_UI_PORT;
var ACCESS_UI_URL = `https://${ACCESS_UI_HOST}`;
ACCESS_UI_URL = ACCESS_UI_PORT
  ? `${ACCESS_UI_URL}:${ACCESS_UI_PORT}`
  : `${ACCESS_UI_URL}`;
// gather all values for the Endpoint and UI configuration
app.locals.endpointConfig = {
  ACCESS_ENDPOINT_HOST: ACCESS_ENDPOINT_HOST,
  ACCESS_ENDPOINT_PORT: ACCESS_ENDPOINT_PORT,
  ACCESS_ENDPOINT_URL: ACCESS_ENDPOINT_URL,
  ACCESS_UI_HOST: ACCESS_UI_HOST,
  ACCESS_UI_PORT: ACCESS_UI_PORT,
  ACCESS_UI_URL: ACCESS_UI_URL,
  HOSTNAME: os.hostname(),
  PLATFORM: os.version().concat(" v", os.release(), ", ", os.machine()),
};

// configure app
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
// const corsOptions = {
//   origin: [`http://localhost/`],
// };
// app.use(cors(corsOptions));
app.use(cors()); 
// app.use(cors({ origin: /http:\/\/localhost/ }));
// app.options("*", cors());
app.use("/static", express.static(path.join(__dirname, 'public')));

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "pug");

/**
 * Configure 'morgan' HTTP request logger middleware.
 * 'dev': Concise output colored by response status for development use.
 */
app.use(logger("dev"));

/**
 * Assign all roots and router pairs with a
 * loop over an array of objects.
/** @type {*} */
const mapRouters = [
  { root: "/", router: require("./routes/home") },
  { root: "/categories", router: require("./routes/categories") }, // JSON Library Specification Categories
  { root: "/executionspecs", router: require("./routes/executionspecs") },
  { root: "/hosts", router: require("./routes/hosts") },
  { root: "/datasets", router: require("./routes/datasets") },
  // { root: "/querysets", router: require("./routes/querysets") },
  { root: "/reportspecs", router: require("./routes/reportspecs") },
  // { root: "/reportsources", router: require("./routes/reportsources") },
  // { root: "/workloads", router: require("./routes/workloads") },
  // { root: "/admin", router: require("./routes/admin") },
];

for (rootPair of mapRouters) {
  app.use(rootPair.root, rootPair.router);
}

const port = process.env.UI_LISTENING_PORT || 5001;
app.listen(port, () => {
  console.log(
    `${packageJson.description} v${packageJson.version} is up and running at port ${port}!`
  );
});
