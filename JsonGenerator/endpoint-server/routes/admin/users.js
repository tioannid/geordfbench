var express = require("express");
var router = express.Router();
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const { getPool } = require("../../db/pg_database");

// get a list of users
router.get("/", async (req, res) => {
  var pool = await getPool();
  const poolResult = await pool.query('SELECT * FROM ENDPOINT."USER"');
  res.json(poolResult.rows);
});

// get a specific user based on the JWT token he presents
// in the req.headers["authorization"] in the form: Bearer TOKEN
router.get("/token", authenticateToken, async (req, res) => {
  // the middleware authenticateToken has made sure that during
  // verification of the user token against the ACCESS_TOKEN_SECRET
  // to populate the req.user field in with the user object who held the JWT token,
  // however an extra field 'iat' (JWT issue time) is added and we need
  // destructure req.user in order to get the actual record persisted in
  // the database.
  const { iat, ...persistedUser } = req.user;
  res.json(persistedUser);
});

// create a new user
// Assumptions: the user { name: , password: } is provided in the req.body
router.post("/", async (req, res) => {
  const username = req.body.name;
  try {
    var pool = await getPool();
    // first check if the user exists!
    // This check is less elegant and more verbose trying to proactively stop duplicate
    // insertions however it does not create gaps in the sequence numbers for 'id'
    const usersResult = await pool.query(
      `SELECT COUNT(*) AS CNT FROM ENDPOINT."USER" WHERE name='${username}'`
    );
    if (usersResult.rows[0].cnt != 0) {
      return res
        .status(400)
        .send(`User '${username}' already exists! (Client side check)`);
    }
    // IF USER DOES NOT EXIST IN THE DB THEN PROCEED

    // generate a default length of (10) salt rounds
    const salt = await bcrypt.genSalt();
    // hash the user password using also the salt
    const hashedPassword = await bcrypt.hash(req.body.password, salt);
    // create a user object with the same user name but with the hashed password
    // instead of the clear text password
    const user = {
      instime: Date.now(),
      description: req.body.description,
      name: username,
      password: hashedPassword,
      salt: salt,
    };

    // store the new user in the database
    const insSql = `INSERT INTO ENDPOINT."USER"(instime, description, name, passwd, salt) VALUES(CURRENT_TIMESTAMP(3), '${user.description}', '${username}', '${user.password}', '${user.salt}')`;
    // const values = Object.values(user);
    const insResult = await pool.query(insSql);
    // first check whether the INSERT of 1 record succeeded
    if (insResult.rowCount !== 1) {
      return res
        .status(400)
        .send(`User '${username}' has not been inserted into the database!`);
    }
    // since the first check is passed we can try to retrieve the persisted record
    // with its auto generated id
    const selSql = `SELECT * FROM ENDPOINT."USER" WHERE name = '${username}'`;
    const selResult = await pool.query(selSql);
    if (selResult.rows.length === 0) {
      // second check whether the INSERT of 1 record succeeded
      return res
        .status(400)
        .send(`User '${username}' has not been inserted into the database!`);
    } else if (selResult.rows.length > 1) {
      // this should not happen
      return res
        .status(400)
        .send(
          `ALERT! More than one user '${username}' has been found in the database! SHOULD NOT OCCUR UNDER ANY CIRCUMSTANCES`
        );
    }
    const persistedUser = selResult.rows[0];
    // send a status of 200: The request succeeded
    res.redirect("/endpointui/users");
    // res.status(200).send(persistedUser);
  } catch (err) {
    // This check is effective enough to stop duplicate insertions
    // however we have gaps in the sequence numbers for 'id'
    if (err.code === "23505") {
      return res
        .status(500)
        .send(
          `User '${username}' already exists! (Unique or Primary key violation reported by the database)`
        );
    }
    // otherwise send a status of 500: Internal Server Error
    res.status(500).send(err);
  }
});

// login an existing user
// Assumptions: the user { name: , password: } is provided in the req.body
router.post("/login", async (req, res) => {
  const username = req.body.name;
  // authenticate user with name and password
  try {
    var pool = await getPool();
    // first check if the user exists!
    // This check is less elegant and more verbose trying to proactively stop duplicate
    // insertions however it does not create gaps in the sequence numbers for 'id'
    const usersResult = await pool.query(
      `SELECT * FROM ENDPOINT."USER" WHERE name='${username}'`
    );
    if (usersResult.rows.length === 0) {
      return res.status(400).send(`User '${username}' does not exist!`);
    } else if (usersResult.rows.length > 1) {
      return res
        .status(400)
        .send(
          `ALERT! More than one user '${username}' has been found in the database! SHOULD NOT OCCUR UNDER ANY CIRCUMSTANCES`
        );
    }
    // IF USER DOES EXIST IN THE DB THEN PROCEED
    const user = usersResult.rows[0];
    // compare the hashed password retrieved from the database with
    // the user provided clear text password
    const match = await bcrypt.compare(req.body.password, user.passwd);
    if (match) {
      // log a success message in the console for debugging purposes
      console.log("Success");
    } else {
      // passwords do not match, therefore we assume that the user
      // does not exist in the database
      return res
        .status(400)
        .send(`User '${user.name}' credentials\' error. Access not allowed!`);
    }
    // sign and return access token with JSON Web Token (JWT)
    const accessToken = jwt.sign(user, ACCESS_TOKEN_SECRET);
    return res.json({ accessToken: accessToken });
  } catch (err) {
    // otherwise send a status of 500: Internal Server Error
    return res.status(500).send(err);
  }
});

// middleware function which authenticates the JWT user token
// Assumptions: the req must have an "authorization" header property
// Outcome:  Since this is a middleware we do not want to tamper with the res
// object. We do however, if the JWT token is valid, have to populate the
// req.user property with the actual user values retrieved from the JWT token
function authenticateToken(req, res, next) {
  // get the authorization data from the request headers
  const authHeader = req.headers["authorization"];
  // authorization is a string of the form: Bearer TOKEN
  // if we have an authHeader then
  // get the TOKEN which is the second element after splitting with the SPACE separator
  const token = authHeader && authHeader.split(" ")[1];
  // "unauthenticated", the user does not have valid authentication credentials for the target resource
  if (token == null) return res.sendStatus(401);
  // verify received token against ACCESS_TOKEN_SECRET
  jwt.verify(token, ACCESS_TOKEN_SECRET, (err, user) => {
    if (err) return res.sendStatus(403); // not a valid token
    req.user = user;
  });
  // standard thing to do in all middleware functions
  next();
}

module.exports = router;
