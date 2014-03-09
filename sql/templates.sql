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

#-- Templates
#INSERT INTO Templates(template_title, template_body) VALUES (?,?)

#--Verificatie
INSERT INTO Templates(template_title, template_body) VALUES (
"Verificatie", 
"Beste %user_firstname% %user_lastname%,<br>
<br>
Om uw e-mailadres te controleren vragen we u om op onderstaande link te klikken:<br>
%verification_url% <br>
<br>
Met vriendelijke groeten,<br>
Dégage");

INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "user_firstname";
INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "user_lastname";
INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Verificatie" AND template_tag_body = "verification_url";

#--Welkom
INSERT INTO Templates(template_title, template_body) VALUES (
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
INSERT INTO Templates(template_title, template_body) VALUES (
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
INSERT INTO Templates(template_title, template_body) VALUES (
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
INSERT INTO Templates(template_title, template_body) VALUES (
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

#--Wachtwoord reset

INSERT INTO Templates(template_title, template_body) VALUES (
"Wachtwoord reset", 
"Beste %user_firstname% %user_lastname%,<br>

Klik op onderstaande link om een nieuw wachtwoord te kiezen.<br>
%password_reset_url%
<br>
Met vriendelijke groeten,<br>
Dégage");

INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_firstname";
INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "user_lastname";
INSERT INTO TemplateTagAssociations(template_id, template_tag_id)
SELECT template_id, template_tag_id FROM Templates, TemplateTags WHERE template_title = "Wachtwoord reset" AND template_tag_body = "password_reset_url";