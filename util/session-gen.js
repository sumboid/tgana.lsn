const { TelegramClient } = require("telegram");
const { StringSession } = require("telegram/sessions");
const { config } = require("dotenv");
const input = require("input");

config();
const apiId = Number(process.env.TELEGRAM_APP_ID);
const apiHash = process.env.TELEGRAM_APP_HASH;
const stringSession = new StringSession("");

(async () => {
  console.log("Loading interactive example...");
  const client = new TelegramClient(stringSession, apiId, apiHash, {
    connectionRetries: 5,
  });
  await client.start({
    phoneNumber: () => input.text("Please enter your number: "),
    password: () => input.text("Please enter your password: "),
    phoneCode: () => input.text("Please enter the code you received: "),
    onError: (err) => console.log(err),
  });
  console.log("You should now be connected.");
  console.log(stringSession.save());
})();
