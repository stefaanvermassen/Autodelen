  #-- TemplateTags
  #-INSERT INTO TemplateTags(template_tag_body) VALUE (?)
  INSERT INTO TemplateTags(template_tag_body) VALUE ("user_firstname");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("user_lastname");

  INSERT INTO TemplateTags(template_tag_body) VALUE ("verification_url");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("password_reset_url");

  INSERT INTO TemplateTags(template_tag_body) VALUE ("infosession_date");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("infosession_address");

  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_from");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_to");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_user_firstname");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_user_lastname");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_url");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_car_address");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("reservation_reason");

  INSERT INTO TemplateTags(template_tag_body) VALUE ("comment");

  INSERT INTO TemplateTags(template_tag_body) VALUE ("car_name");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("amount");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("car_cost_description");
  INSERT INTO TemplateTags(template_tag_body) VALUE ("car_cost_time");

  #-- Templates
  #INSERT INTO Templates(template_id, template_title, template_body) VALUES (?,?)

  #--Verificatie
  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  1,
  "Verificatie",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Om uw e-mailadres te controleren vragen we u om op onderstaande link te klikken:<br>
  %verification_url% <br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "verification_url";

  #--Welkom
  INSERT INTO Templates(template_id, template_title, template_body) VALUES (
  2,
  "Welkom",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Welkom bij Dégage!<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Welkom" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Welkom" AND template_tag_body = "user_lastname";

  #--Infosessie ingeschreven
  INSERT INTO Templates(template_id, template_title, template_body) VALUES (
  3,
  "Infosessie ingeschreven",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  U heeft zich ingeschreven voor een infosessie op %infosession_date%. <br>
  Deze infosessie zal doorgaan op het volgende adres:<br>
  %infosession_address%<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "infosession_address";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Infosessie ingeschreven" AND template_tag_body = "infosession_date";

  #--Reservatie bevestigen
  INSERT INTO Templates(template_id, template_title, template_body) VALUES (
  4,
  "Reservatie bevestigen",
  "Beste %user_firstname% %user_lastname%,<br>
  <br>
  Iemand wilt uw auto reserven van %reservation_from% tot %reservation_to%.<br>
  <br>
  Gelieve deze reservatie zo snel mogelijk goed te keuren. Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_from";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_to";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigen" AND template_tag_body = "reservation_url";

  #--Reservatie bevestigd
  INSERT INTO Templates(template_id, template_title, template_body) VALUES (
  5,
  "Reservatie bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw reservatie is bevestigd, de auto is gereserveerd van %reservation_from% tot %reservation_to%.<br>
  <br>
  Adres van de auto:<br>
  %reservation_car_address%<br>
  <br>
  Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_from";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_to";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_car_address";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie bevestigd" AND template_tag_body = "reservation_url";

  #--Reservatie geweigerd
  INSERT INTO Templates(template_id, template_title, template_body) VALUES (
  6,
  "Reservatie geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw reservatie, van %reservation_from% tot %reservation_to%, werd geweigerd door de eigenaar om volgende reden:<br>
  <br>
  <i>%reservation_reason%</i><br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage");

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_from";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_to";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Reservatie geweigerd" AND template_tag_body = "reservation_reason";

  #--Wachtwoord reset

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  7,
  "Wachtwoord reset",
  "Beste %user_firstname% %user_lastname%,<br>

  Klik op onderstaande link om een nieuw wachtwoord te kiezen.<br>
  %password_reset_url%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "password_reset_url";

 #-- Algemene voorwaarden
  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
    8,
    "Algemene voorwaarden",
    "TODO: Aanvullen algemene voorwaarden in templates", 0);

  #--Lidmaatschap bevestigd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  9,
  "Lidmaatschap bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Gefeliciteerd! Uw lidmaatschap bij Dégage werd zonet bevestgd.<br>
  %comment%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap bevestigd" AND template_tag_body = "comment";

  #--Lidmaatschap geweigerd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  10,
  "Lidmaatschap geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw lidmaatschap bij Dégage werd zonet geweigerd.<br>
  %comment%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Lidmaatschap geweigerd" AND template_tag_body = "comment";

  #--Autokost bevestigd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  11,
  "Autokost bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw autokost werd zonet bevestigd door een admin.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_cost_description";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "amount";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost bevestigd" AND template_tag_body = "car_cost_time";

  #--Autokost geweigerd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  12,
  "Autokost geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw autokost werd helaas geweigerd door een admin.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_cost_description";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "amount";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost geweigerd" AND template_tag_body = "car_cost_time";


  #--Tankbeurt bevestigd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  13,
  "Tankbeurt bevestigd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw tankbeurt werd zonet bevestigd door de auto-eigenaar.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt bevestigd" AND template_tag_body = "amount";


   #--Tankbeurt geweigerd

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  14,
  "Tankbeurt geweigerd",
  "Beste %user_firstname% %user_lastname%,<br>

  Uw tankbeurt werd helaas geweigerd door de auto-eigenaar.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt geweigerd" AND template_tag_body = "amount";

  #-- Reminder mail
  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  15,
  "Ongelezen berichten",
  "Beste %user_firstname% %user_lastname%,<br>

  U heeft ongelezen berichten. Gelieve in te loggen op uw Dégage-account.<br>
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 0);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Ongelezen berichten" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Ongelezen berichten" AND template_tag_body = "user_lastname";

  #--Tankbeurt aanvraag

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  16,
  "Tankbeurt aanvraag",
  "Beste %user_firstname% %user_lastname%,<br>

  Er werd zonet een tankbeurt ingegeven voor uw wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>
  %car_name%
  <br>
  %amount%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Tankbeurt aanvraag" AND template_tag_body = "amount";

  #--Autokost aanvraag

  INSERT INTO Templates(template_id, template_title, template_body, template_send_mail_changeable) VALUES (
  17,
  "Autokost aanvraag",
  "Beste %user_firstname% %user_lastname%,<br>

  Er werd zonet een autokost ingegeven voor de volgende wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>
  %car_name%
  <br>
  %car_cost_description%
  <br>
  %amount%
  <br>
  %car_cost_time%
  <br>
  Met vriendelijke groeten,<br>
  Dégage", 1);

  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "user_firstname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "user_lastname";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_name";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_cost_description";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "amount";
  INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
  SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Autokost aanvraag" AND template_tag_body = "car_cost_time";