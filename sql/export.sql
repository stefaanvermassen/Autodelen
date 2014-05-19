--
-- MySQL 5.6.16
-- Mon, 19 May 2014 14:41:31 +0000
--

CREATE TABLE `Receipts` (
   `receipt_id` int(11) not null auto_increment,
   `receipt_name` char(32) not null,
   `receipt_date` datetime,
   `receipt_fileID` int(11),
   `receipt_userID` int(11) not null,
   `receipt_price` decimal(19,4),
   PRIMARY KEY (`receipt_id`),
   KEY `receipt_fileID` (`receipt_fileID`),
   KEY `receipt_userID` (`receipt_userID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `Receipts` is empty]

CREATE TABLE `addresses` (
   `address_id` int(10) not null auto_increment,
   `address_country` varchar(64) not null default 'Belgium',
   `address_city` varchar(64) not null,
   `address_zipcode` varchar(12) not null,
   `address_street` varchar(64) not null default '0',
   `address_street_number` varchar(8) not null default '0',
   `address_street_bus` varchar(4),
   `address_created_at` datetime,
   `address_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`address_id`),
   KEY `address_place_zip` (`address_city`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=5;

INSERT INTO `addresses` (`address_id`, `address_country`, `address_city`, `address_zipcode`, `address_street`, `address_street_number`, `address_street_bus`, `address_created_at`, `address_updated_at`) VALUES 
('1', 'België', 'Gent', '9000', 'Krijgslaan', '281', '', '2014-05-19 16:13:31', '2014-05-19 16:13:31'),
('2', 'België', 'Gent', '9000', 'Krijgslaan', '281', '', '2014-05-19 16:14:04', '2014-05-19 16:33:03'),
('3', 'België', 'Gent', '9000', 'Stalhof', '6', '', '2014-05-19 16:36:35', '2014-05-19 16:36:35'),
('4', 'België', 'Gent', '9000', 'Stalhof', '6', '', '2014-05-19 16:36:53', '2014-05-19 16:36:53');

CREATE TABLE `approvals` (
   `approval_id` int(11) not null auto_increment,
   `approval_user` int(11),
   `approval_admin` int(11),
   `approval_submission` datetime not null,
   `approval_date` datetime,
   `approval_status` enum('PENDING','ACCEPTED','DENIED') not null default 'PENDING',
   `approval_infosession` int(11),
   `approval_user_message` text,
   `approval_admin_message` text,
   PRIMARY KEY (`approval_id`),
   KEY `FK_approval_user` (`approval_user`),
   KEY `FK_approval_admin` (`approval_admin`),
   KEY `FK_approval_session` (`approval_infosession`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `approvals` is empty]

CREATE TABLE `caravailabilities` (
   `car_availability_id` int(11) not null auto_increment,
   `car_availability_car_id` int(11) not null,
   `car_availability_begin_day_of_week` int(11) not null,
   `car_availability_begin_time` time not null,
   `car_availability_end_day_of_week` int(11) not null,
   `car_availability_end_time` time not null,
   PRIMARY KEY (`car_availability_id`),
   KEY `car_availability_car_id` (`car_availability_car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `caravailabilities` is empty]

CREATE TABLE `carcosts` (
   `car_cost_id` int(11) not null auto_increment,
   `car_cost_car_id` int(11) not null,
   `car_cost_proof` int(11),
   `car_cost_amount` decimal(19,4) not null,
   `car_cost_description` text,
   `car_cost_status` enum('REQUEST','ACCEPTED','REFUSED') not null default 'REQUEST',
   `car_cost_time` datetime,
   `car_cost_mileage` decimal(10,1),
   `car_cost_billed` date,
   `car_cost_created_at` datetime,
   `car_cost_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`car_cost_id`),
   KEY `car_cost_car_id` (`car_cost_car_id`),
   KEY `car_cost_proof` (`car_cost_proof`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `carcosts` is empty]

CREATE TABLE `carinsurances` (
   `insurance_id` int(11) not null auto_increment,
   `insurance_name` varchar(64),
   `insurance_expiration` date,
   `insurance_contract_id` int(11),
   `insurance_bonus_malus` int(11),
   `insurance_created_at` datetime,
   `insurance_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`insurance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `carinsurances` is empty]

CREATE TABLE `carprivileges` (
   `car_privilege_user_id` int(11) not null,
   `car_privilege_car_id` int(11) not null,
   PRIMARY KEY (`car_privilege_user_id`,`car_privilege_car_id`),
   KEY `car_privilege_car_id` (`car_privilege_car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- [Table `carprivileges` is empty]

CREATE TABLE `carreservations` (
   `reservation_id` int(11) not null auto_increment,
   `reservation_status` enum('REQUEST','ACCEPTED','REFUSED','CANCELLED','REQUEST_DETAILS','DETAILS_PROVIDED','FINISHED') not null default 'REQUEST',
   `reservation_car_id` int(11) not null,
   `reservation_user_id` int(11) not null,
   `reservation_from` timestamp not null default '0000-00-00 00:00:00',
   `reservation_to` timestamp not null default '0000-00-00 00:00:00',
   `reservation_message` varchar(128),
   `reservation_created_at` datetime,
   `reservation_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`reservation_id`),
   KEY `reservation_car_id` (`reservation_car_id`),
   KEY `reservation_user_id` (`reservation_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `carreservations` is empty]

CREATE TABLE `carrides` (
   `car_ride_car_reservation_id` int(11) not null,
   `car_ride_status` bit(1) not null default 'b'0'',
   `car_ride_start_mileage` decimal(10,1),
   `car_ride_end_mileage` decimal(10,1),
   `car_ride_damage` bit(1) not null default 'b'0'',
   `car_ride_refueling` int(11) not null,
   `car_ride_cost` decimal(19,4),
   `car_ride_billed` date,
   `car_ride_created_at` datetime,
   `car_ride_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`car_ride_car_reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- [Table `carrides` is empty]

CREATE TABLE `cars` (
   `car_id` int(11) not null auto_increment,
   `car_name` varchar(64) not null,
   `car_type` varchar(64) not null default '0',
   `car_brand` varchar(64) not null default '0',
   `car_location` int(11),
   `car_seats` tinyint(3) unsigned,
   `car_doors` tinyint(3) unsigned,
   `car_year` int(11),
   `car_manual` bit(1) not null default 'b'0'',
   `car_gps` bit(1) not null default 'b'0'',
   `car_hook` bit(1) not null default 'b'0'',
   `car_fuel` enum('PETROL','DIESEL','BIODIESEL','GAS','HYBRID','ELECTRIC'),
   `car_fuel_economy` int(11),
   `car_estimated_value` int(11),
   `car_owner_annual_km` int(11),
   `car_technical_details` int(11),
   `car_insurance` int(11),
   `car_owner_user_id` int(11) not null,
   `car_comments` varchar(256),
   `car_active` bit(1) not null default 'b'0'',
   `car_images_id` int(11),
   `car_created_at` datetime,
   `car_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`car_id`),
   KEY `car_owner_user_id` (`car_owner_user_id`),
   KEY `car_location` (`car_location`),
   KEY `car_images_id` (`car_images_id`),
   KEY `car_technical_details` (`car_technical_details`),
   KEY `car_insurance` (`car_insurance`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=2;

INSERT INTO `cars` (`car_id`, `car_name`, `car_type`, `car_brand`, `car_location`, `car_seats`, `car_doors`, `car_year`, `car_manual`, `car_gps`, `car_hook`, `car_fuel`, `car_fuel_economy`, `car_estimated_value`, `car_owner_annual_km`, `car_technical_details`, `car_insurance`, `car_owner_user_id`, `car_comments`, `car_active`, `car_images_id`, `car_created_at`, `car_updated_at`) VALUES 
('1', 'StefaanVroem', 'S400', 'Mercedes', '4', '5', '4', '2013', '0', '1', '0', 'DIESEL', '10', '25000', '1000', '', '', '1', '', '1', '1', '2014-05-19 16:36:53', '2014-05-19 16:36:53');

CREATE TABLE `damagelogs` (
   `damage_log_id` int(11) not null auto_increment,
   `damage_log_damage_id` int(11) not null,
   `damage_log_description` text,
   `damage_log_created_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`damage_log_id`),
   KEY `damage_log_damage_id` (`damage_log_damage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `damagelogs` is empty]

CREATE TABLE `damages` (
   `damage_id` int(11) not null auto_increment,
   `damage_car_ride_id` int(11) not null,
   `damage_filegroup_id` int(11),
   `damage_description` text,
   `damage_finished` bit(1) not null default 'b'0'',
   `damage_time` datetime not null,
   `damage_created_at` datetime,
   `damage_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`damage_id`),
   KEY `damage_car_ride_id` (`damage_car_ride_id`),
   KEY `damage_filegroup_id` (`damage_filegroup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `damages` is empty]

CREATE TABLE `filegroups` (
   `file_group_id` int(11) not null auto_increment,
   PRIMARY KEY (`file_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `filegroups` is empty]

CREATE TABLE `files` (
   `file_id` int(11) not null auto_increment,
   `file_path` varchar(255) not null,
   `file_name` varchar(128),
   `file_content_type` varchar(64),
   `file_file_group_id` int(11),
   `file_created_at` datetime,
   `file_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`file_id`),
   KEY `file_file_group_id` (`file_file_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=2;

INSERT INTO `files` (`file_id`, `file_path`, `file_name`, `file_content_type`, `file_file_group_id`, `file_created_at`, `file_updated_at`) VALUES 
('1', 'carphotos/dab8113f-d4e6-4341-9000-dd21cd412652-Mercedes-S-Klasse-2013-Preis-Luxus-Limousine-tL.jpg', 'Mercedes-S-Klasse-2013-Preis-Luxus-Limousine-tL.jpg', 'image/jpeg', '', '2014-05-19 16:36:53', '2014-05-19 16:36:53');

CREATE TABLE `infosessionenrollees` (
   `infosession_id` int(11) not null,
   `infosession_enrollee_id` int(11) not null,
   `infosession_enrollment_status` enum('ENROLLED','PRESENT','ABSENT') not null default 'ENROLLED',
   PRIMARY KEY (`infosession_id`,`infosession_enrollee_id`),
   KEY `infosession_enrollee_id` (`infosession_enrollee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- [Table `infosessionenrollees` is empty]

CREATE TABLE `infosessions` (
   `infosession_id` int(11) not null auto_increment,
   `infosession_type` enum('NORMAL','OWNER','OTHER') not null default 'NORMAL',
   `infosession_type_alternative` varchar(64),
   `infosession_timestamp` timestamp not null default '0000-00-00 00:00:00',
   `infosession_address_id` int(11) not null,
   `infosession_host_user_id` int(11),
   `infosession_max_enrollees` int(11),
   `infosession_comments` varchar(256),
   `infosession_created_at` datetime,
   `infosession_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`infosession_id`),
   KEY `infosession_host_user_id` (`infosession_host_user_id`),
   KEY `infosession_address_id` (`infosession_address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=3;

INSERT INTO `infosessions` (`infosession_id`, `infosession_type`, `infosession_type_alternative`, `infosession_timestamp`, `infosession_address_id`, `infosession_host_user_id`, `infosession_max_enrollees`, `infosession_comments`, `infosession_created_at`, `infosession_updated_at`) VALUES 
('1', 'NORMAL', '', '2014-05-20 14:00:00', '1', '1', '10', '', '2014-05-19 16:13:31', '2014-05-19 16:13:31'),
('2', 'OWNER', '', '2014-05-27 12:00:00', '2', '1', '5', '', '2014-05-19 16:14:04', '2014-05-19 16:20:44');

CREATE TABLE `jobs` (
   `job_id` bigint(20) not null auto_increment,
   `job_type` enum('IS_REMINDER','RES_REMINDER','REPORT','RESERVE_ACCEPT','DRIVE_FINISH') not null default 'REPORT',
   `job_ref_id` int(11) default '0',
   `job_time` datetime not null,
   `job_finished` bit(1) not null default 'b'0'',
   PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=4;

INSERT INTO `jobs` (`job_id`, `job_type`, `job_ref_id`, `job_time`, `job_finished`) VALUES 
('1', 'IS_REMINDER', '1', '2014-05-19 14:00:00', '1'),
('2', 'IS_REMINDER', '2', '2014-05-26 12:00:00', '0'),
('3', 'REPORT', '0', '2014-06-01 00:00:00', '0');

CREATE TABLE `messages` (
   `message_id` int(11) not null auto_increment,
   `message_from_user_id` int(11) not null,
   `message_to_user_id` int(11) not null,
   `message_read` bit(1) not null default 'b'0'',
   `message_subject` varchar(255) not null default 'Bericht van een Dégage-gebruiker',
   `message_body` text not null,
   `message_created_at` datetime,
   `message_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`message_id`),
   KEY `message_from_user_id` (`message_from_user_id`),
   KEY `message_to_user_id` (`message_to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `messages` is empty]

CREATE TABLE `notifications` (
   `notification_id` int(11) not null auto_increment,
   `notification_user_id` int(11) not null,
   `notification_read` bit(1) not null default 'b'0'',
   `notification_subject` varchar(255),
   `notification_body` text,
   `notification_created_at` datetime,
   `notification_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`notification_id`),
   KEY `notification_user_id` (`notification_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `notifications` is empty]

CREATE TABLE `refuels` (
   `refuel_id` int(11) not null auto_increment,
   `refuel_car_ride_id` int(11) not null,
   `refuel_file_id` int(11),
   `refuel_amount` decimal(19,4),
   `refuel_status` enum('CREATED','REQUEST','ACCEPTED','REFUSED') not null default 'CREATED',
   `refuel_billed` date,
   `refuel_created_at` datetime,
   `refuel_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`refuel_id`),
   KEY `refuel_car_ride_id` (`refuel_car_ride_id`),
   KEY `refuel_file_id` (`refuel_file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `refuels` is empty]

CREATE TABLE `settings` (
   `setting_id` int(11) not null auto_increment,
   `setting_name` char(32) not null,
   `setting_value` varchar(256),
   `setting_after` datetime,
   PRIMARY KEY (`setting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=15;

INSERT INTO `settings` (`setting_id`, `setting_name`, `setting_value`, `setting_after`) VALUES 
('1', 'show_profile', 'true', ''),
('2', 'deprecation_cost', '0.8', ''),
('3', 'cost_levels', '3', ''),
('4', 'cost_limit_0', '100', ''),
('5', 'cost_limit_1', '200', ''),
('6', 'cost_0', '0.31', ''),
('7', 'cost_1', '0.26', ''),
('8', 'cost_2', '0.21', ''),
('9', 'show_maps', 'true', ''),
('10', 'infosessions_page_size', '10', ''),
('11', 'scheduler_interval', '300', ''),
('12', 'infosession_reminder', '1440', ''),
('13', 'reservation_auto_accept', '4320', ''),
('14', 'maps_tile_server', 'http://tile.openstreetmap.org/%d/%d/%d.png', '');

CREATE TABLE `technicalcardetails` (
   `details_id` int(11) not null auto_increment,
   `details_car_license_plate` varchar(64),
   `details_car_registration` int(11),
   `details_car_chassis_number` int(17),
   `details_created_at` datetime,
   `details_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`details_id`),
   UNIQUE KEY (`details_car_license_plate`,`details_car_chassis_number`),
   KEY `details_car_registration` (`details_car_registration`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- [Table `technicalcardetails` is empty]

CREATE TABLE `templates` (
   `template_id` int(11) not null,
   `template_title` varchar(255) not null,
   `template_subject` varchar(255) not null default 'Bericht van Dégage!',
   `template_body` text not null,
   `template_send_mail` bit(1) not null default 'b'1'',
   `template_send_mail_changeable` bit(1) not null default 'b'1'',
   `template_created_at` datetime,
   `template_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`template_id`),
   UNIQUE KEY (`template_title`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `templates` (`template_id`, `template_title`, `template_subject`, `template_body`, `template_send_mail`, `template_send_mail_changeable`, `template_created_at`, `template_updated_at`) VALUES 
('1', 'Verificatie', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n  <br>\n  Om jouw e-mailadres te controleren vragen we je om op onderstaande link te klikken:<br>\n  %verification_url% <br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '0', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('2', 'Welkom', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n  <br>\n  Welkom bij Dégage!<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '0', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('3', 'Infosessie ingeschreven', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n  <br>\n  Je hebt je ingeschreven voor een infosessie op %infosession_date%. <br>\n  Deze infosessie zal doorgaan op het volgende adres:<br>\n  %infosession_address%<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('4', 'Reservatie bevestigen', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n  <br>\n  %reservation_user_firstname% %reservation_user_lastname% wil jouw auto reserven van %reservation_from% tot %reservation_to%.<br>\n  <br>\n  Gelieve deze reservatie zo snel mogelijk goed te keuren. Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>\n  <br>\n  Commentaar van de lener: %comment%.\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('5', 'Reservatie bevestigd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw reservatie is bevestigd, de auto is gereserveerd van %reservation_from% tot %reservation_to%.<br>\n  <br>\n  Adres van de auto:<br>\n  %reservation_car_address%<br>\n  <br>\n  Opmerkingen door de eigenaar:<br>\n  <br>\n  <i>%reservation_remarks%</i><br>\n  <br>\n  Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('6', 'Reservatie geweigerd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw reservatie, van %reservation_from% tot %reservation_to%, werd geweigerd door de eigenaar om volgende reden:<br>\n  <br>\n  <i>%reservation_reason%</i><br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('7', 'Wachtwoord reset', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Klik op onderstaande link om een nieuw wachtwoord te kiezen.<br>\n  %password_reset_url%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '0', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('8', 'Algemene voorwaarden', 'Bericht van Dégage!', '<div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Autodelensysteem&nbsp;</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">In ons systeem zijn er mensen mét auto en mensen zonder auto. Toch hebben ze allemaal een auto ter beschikking als ze dat willen (en op tijd reserveren). Eén auto wordt dus door verschillende mensen gebruikt.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Contract&nbsp;</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Als gebruiker bij Dégage! onderteken je een contract, waarin alles wat op deze website uitgelegd wordt, nog eens duidelijk vermeld staat. Je verklaart je dus akkoord met de regeling.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Eigenaars</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">schieten alle kosten voor hun auto voor en zijn verantwoordelijk voor verzekering, taksen, onderhoud</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">zorgen ervoor dat die verzekering, taksen in orde zijn en dat de auto in goede staat verkeert</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">krijgen een vergoeding per kilometer dat de wagen door iemand anders gebruikt wordt en kunnen zo de gemaakte kosten terugwinnen.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">zijn omnium verzekerd door het interne Dégage! waarborgensysteem</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">regelen zelf de reservaties van hun wagen</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Gebruikers</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">betalen een kilometervergoeding die overeenkomt met de reële kosten per kilometer;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">betalen een waarborg maar geen abonnementsgeld;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">behandelen de auto als een \'goede huisvader\' (m/v);</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">houden zich aan het contract dat ze ondertekenden;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">blijven minimaal 1 jaar lid;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">reserveren een auto als ze die nodig hebben en spreken af met de eigenaar voor sleutel;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">vullen na gebruik het bonnetje in;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Goede huisvader (m/v)</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"> Dit betekent dat men als een normaal voorzichtig mens de goederen behoudt en beheert en de wet respecteert. De hoofdverplichting van de gebruiker is dat de auto in dezelfde staat moet worden teruggegeven.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Euvels en gebreken meldt de gebruiker aan de eigenaar.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">De gebruiker respecteert de verkeersregels. Als hij die overtreedt, moet hij en hij alleen de boete betalen.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Een goede huisvader m/v rijdt niet onder invloed en niet te snel. Als de gebruiker die regel overtreedt en een ongeval heeft, moet hij alle kosten betalen. De verzekering is in dat geval genadeloos: ze kan alle schade aan derden op de gebruiker verhalen. Onnodig te zeggen dat die kosten kunnen oplopen tot een bedrag dat een mens van zijn leven niet kan verdienen.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Elke eigenaar heeft een gebruiksreglement voor zijn auto. Daarin staan regels die de eigenaar belangrijk vindt: of je mag roken bijvoorbeeld en eigenaardigheden van de auto zoals de choke, de radio, welke sleutel voor welk sleutelgat. Gebruikers respecteren dit reglement.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>\'Intern verzekeringssysteem\'</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">bij een schadegeval kleine gebruikers dragen minder bij dan grote gebruikers aangezien hun waarborg lager is</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">hoe meer leden er zijn, hoe groter de som van de waarborgen wordt. Er is veel kans dat slechts een klein deel van je waarborg moet gebruikt worden voor een schadegeval. Anderszijds is het ook zo dat je zelf ook van dit waarborgsysteem kan genieten als je een ongeluk tegenkomt : je moet zelf niet de volledige waarde van de auto aan de eigenaar terugbetalen.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Als er nog geld in reserve is op de Dégage!-rekening, kan dit eventueel ook gebruikt worden voor het weer aanzuiveren van de waarborgen</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Je loopt dus inderdaad het risico (een deel van) je waarborg kwijt te raken. Maar de waarborgsommen zijn zeer democratisch (vinden we zelf) voor het quasi onbeperkt gebruik van meerdere auto\'s.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Particulier&nbsp;</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Er bestaan verschillende systemen om aan autodelen te doen, ook commerciële. Dat is Dégage! in ieder geval niet, er wordt geen winst gemaakt. Binnen de niet-commerciële initiatieven zijn er nog verschillen. Bij de links kan je doorklikken naar sites van andere systemen. Dégage! komt hierop neer: de eigenaars blijven ten allen tijde eigenaar. In sommige andere systemen worden de auto\'s eigendom van het collectief. Niet zo bij ons. Dégage! is een uitgekiend systeem waarbij zowel eigenaars als gebruikers voordeel doen en waarbij een minimum aan administratie en dergelijke komt kijken.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Systeem&nbsp;</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Geleed en geordend geheel, complex, geschikt volgens een ordenend beginsel, syn. stelsel.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Voorschot&nbsp;</b></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Het voorschot werd in 2011 afgeschaft. Betaalde voorschotten worden bij de waarborg gevoegd.</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><br></span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\"><b>Waarborg</b>&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Nieuwe gebruikers betalen bij Dégage! 75 € waarborg als ze lid worden. Na drie maanden wordt die waarborg aangepast aan de hoogte van je verbruik Na die eerste aanpassing wordt de waarborgsom elk jaar aangepast als dat nodig is.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">75 € is het laagste bedrag; voor wie minder dan 500 kilometer per drie maanden rijdt. Wie meer rijdt, betaalt meer.&nbsp;</span></font></div><div><font color=\"#696969\" face=\"Verdana, sans-serif\"><span style=\"font-size: 12px; line-height: normal;\">Gebruikers die het systeem verlaten, krijgen hun waarborg geheel of gedeeltelijk terug. De waarborgsommen worden gebruikt bij ongevallen of andere onvoorziene omstandigheden, als een soort interne verzekering. Als die sommen onaangeroerd blijven, kan Dégage! de volledige waarborg terugbetalen.</span></font></div>', '0', '0', '2014-05-14 14:18:57', '2014-05-14 16:03:41'),
('9', 'Lidmaatschap bevestigd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Gefeliciteerd! Jouw lidmaatschap bij Dégage werd zonet bevestgd.<br>\n  %comment%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('10', 'Lidmaatschap geweigerd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw lidmaatschap bij Dégage werd zonet geweigerd.<br>\n  %comment%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('11', 'Autokost bevestigd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw autokost werd zonet bevestigd door een admin.<br>\n  %car_name%\n  <br>\n  %car_cost_description%\n  <br>\n  %amount%\n  <br>\n  %car_cost_time%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('12', 'Autokost geweigerd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw autokost werd helaas geweigerd door een admin.<br>\n  %car_name%\n  <br>\n  %car_cost_description%\n  <br>\n  %amount%\n  <br>\n  %car_cost_time%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('13', 'Tankbeurt bevestigd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw tankbeurt werd zonet bevestigd door de auto-eigenaar.<br>\n  %car_name%\n  <br>\n  %amount%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('14', 'Tankbeurt geweigerd', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Jouw tankbeurt werd helaas geweigerd door de auto-eigenaar.<br>\n  %car_name%\n  <br>\n  %amount%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('15', 'Ongelezen berichten', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Je hebt ongelezen berichten. Gelieve in te loggen op jouw Dégage-account.<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '0', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('16', 'Tankbeurt aanvraag', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Er werd zonet een tankbeurt ingegeven voor jouw wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>\n  %car_name%\n  <br>\n  %amount%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('17', 'Autokost aanvraag', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  Er werd zonet een autokost ingegeven voor de volgende wagen. Gelieve deze zo snel mogelijk goed te keuren.<br>\n  %car_name%\n  <br>\n  %car_cost_description%\n  <br>\n  %amount%\n  <br>\n  %car_cost_time%\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '0', '0', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('18', 'Contractmanager toegewezen', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n\n  %admin_name% werd zojuist toegewezen als jouw contractverantwoordelijke. Deze persoon zal jouw registratie verder afhandelen.<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('19', 'Ritdetails aangevuld', 'Bericht van Dégage!', 'Beste %user_firstname% %user_lastname%,<br>\n  <br>\n  Er zijn zonet nieuwe ritdetails aangevuld voor uw auto voor de reservatie van %reservation_from% tot %reservation_to% door %reservation_user_firstname% %reservation_user_lastname%<br>\n  <br>\n  Gelieve deze details zo snel mogelijk goed te keuren. Klik <a href=\"%reservation_url%\">hier</a> om naar de reservatie te gaan.<br>\n  <br>\n  Met vriendelijke groeten,<br>\n  Dégage', '1', '1', '2014-05-19 16:07:34', '2014-05-19 16:07:34');

CREATE TABLE `templatetagassociations` (
   `template_tag_association_id` int(11) not null auto_increment,
   `template_tag_id` int(11) not null,
   `template_id` int(11) not null,
   PRIMARY KEY (`template_tag_association_id`),
   KEY `template_id` (`template_id`),
   KEY `template_tag_id` (`template_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=80;

INSERT INTO `templatetagassociations` (`template_tag_association_id`, `template_tag_id`, `template_id`) VALUES 
('1', '1', '1'),
('2', '2', '1'),
('3', '3', '1'),
('4', '1', '2'),
('5', '2', '2'),
('6', '1', '3'),
('7', '2', '3'),
('8', '6', '3'),
('9', '5', '3'),
('10', '1', '4'),
('11', '2', '4'),
('12', '7', '4'),
('13', '8', '4'),
('14', '11', '4'),
('15', '14', '4'),
('16', '9', '4'),
('17', '10', '4'),
('18', '1', '5'),
('19', '2', '5'),
('20', '7', '5'),
('21', '8', '5'),
('22', '12', '5'),
('23', '11', '5'),
('24', '1', '6'),
('25', '2', '6'),
('26', '7', '6'),
('27', '8', '6'),
('28', '13', '6'),
('29', '1', '7'),
('30', '2', '7'),
('31', '4', '7'),
('32', '1', '9'),
('33', '2', '9'),
('34', '14', '9'),
('35', '1', '10'),
('36', '2', '10'),
('37', '14', '10'),
('38', '1', '11'),
('39', '2', '11'),
('40', '15', '11'),
('41', '17', '11'),
('42', '16', '11'),
('43', '18', '11'),
('44', '1', '12'),
('45', '2', '12'),
('46', '15', '12'),
('47', '17', '12'),
('48', '16', '12'),
('49', '18', '12'),
('50', '1', '13'),
('51', '2', '13'),
('52', '15', '13'),
('53', '16', '13'),
('54', '1', '14'),
('55', '2', '14'),
('56', '15', '14'),
('57', '16', '14'),
('58', '1', '15'),
('59', '2', '15'),
('60', '1', '16'),
('61', '2', '16'),
('62', '15', '16'),
('63', '16', '16'),
('64', '1', '17'),
('65', '2', '17'),
('66', '15', '17'),
('67', '17', '17'),
('68', '16', '17'),
('69', '18', '17'),
('70', '1', '9'),
('71', '2', '9'),
('72', '19', '18'),
('73', '1', '19'),
('74', '2', '19'),
('75', '7', '19'),
('76', '8', '19'),
('77', '11', '19'),
('78', '9', '19'),
('79', '10', '19');

CREATE TABLE `templatetags` (
   `template_tag_id` int(11) not null auto_increment,
   `template_tag_body` varchar(255) not null,
   PRIMARY KEY (`template_tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=20;

INSERT INTO `templatetags` (`template_tag_id`, `template_tag_body`) VALUES 
('1', 'user_firstname'),
('2', 'user_lastname'),
('3', 'verification_url'),
('4', 'password_reset_url'),
('5', 'infosession_date'),
('6', 'infosession_address'),
('7', 'reservation_from'),
('8', 'reservation_to'),
('9', 'reservation_user_firstname'),
('10', 'reservation_user_lastname'),
('11', 'reservation_url'),
('12', 'reservation_car_address'),
('13', 'reservation_reason'),
('14', 'comment'),
('15', 'car_name'),
('16', 'amount'),
('17', 'car_cost_description'),
('18', 'car_cost_time'),
('19', 'admin_name');

CREATE TABLE `userroles` (
   `userrole_userid` int(11) not null,
   `userrole_role` enum('SUPER_USER','CAR_OWNER','CAR_USER','INFOSESSION_ADMIN','MAIL_ADMIN','PROFILE_ADMIN','RESERVATION_ADMIN','CAR_ADMIN') not null,
   PRIMARY KEY (`userrole_userid`,`userrole_role`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `userroles` (`userrole_userid`, `userrole_role`) VALUES 
('1', 'SUPER_USER');

CREATE TABLE `users` (
   `user_id` int(11) not null auto_increment,
   `user_email` varchar(64) not null,
   `user_password` char(64) not null,
   `user_firstname` varchar(64) not null,
   `user_lastname` varchar(64) not null,
   `user_gender` enum('MALE','FEMALE','UNKNOWN') not null default 'UNKNOWN',
   `user_cellphone` varchar(16),
   `user_phone` varchar(16),
   `user_address_domicile_id` int(11),
   `user_address_residence_id` int(11),
   `user_driver_license_id` varchar(32),
   `user_driver_license_file_group_id` int(11),
   `user_identity_card_id` varchar(32),
   `user_identity_card_registration_nr` varchar(32),
   `user_identity_card_file_group_id` int(11),
   `user_status` enum('EMAIL_VALIDATING','REGISTERED','FULL_VALIDATING','FULL','BLOCKED','DROPPED','INACTIVE') not null default 'EMAIL_VALIDATING',
   `user_damage_history` text,
   `user_payed_deposit` bit(1),
   `user_agree_terms` bit(1),
   `user_image_id` int(11),
   `user_created_at` datetime,
   `user_last_notified` datetime,
   `user_updated_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`user_id`),
   UNIQUE KEY (`user_email`),
   KEY `user_address_domicile_id` (`user_address_domicile_id`),
   KEY `user_address_residence_id` (`user_address_residence_id`),
   KEY `user_driver_license_file_group_id` (`user_driver_license_file_group_id`),
   KEY `user_identity_card_file_group_id` (`user_identity_card_file_group_id`),
   KEY `user_image_id` (`user_image_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=7;

INSERT INTO `users` (`user_id`, `user_email`, `user_password`, `user_firstname`, `user_lastname`, `user_gender`, `user_cellphone`, `user_phone`, `user_address_domicile_id`, `user_address_residence_id`, `user_driver_license_id`, `user_driver_license_file_group_id`, `user_identity_card_id`, `user_identity_card_registration_nr`, `user_identity_card_file_group_id`, `user_status`, `user_damage_history`, `user_payed_deposit`, `user_agree_terms`, `user_image_id`, `user_created_at`, `user_last_notified`, `user_updated_at`) VALUES 
('1', 'stefaanvermassen@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Stefaan', 'Vermassen', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('2', 'cedricvangoethem@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Cedric', 'Van Goethem', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('3', 'marynabardadym@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Maryna', 'Bardadym', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('4', 'hannesmareen@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Hannes', 'Mareen', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('5', 'benjamintwechuizen@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Benjamin', 'Twechuizen', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34'),
('6', 'karstengoossens@zelensis.be', '$2a$12$yxnPiLMK9/AgAf9T.xpRlOLySyKXHZwYMREm3R8cDc8TekQTP5HBy', 'Karsten', 'Goossens', 'UNKNOWN', '', '', '', '', '', '', '', '', '', 'REGISTERED', '', '', '', '', '2014-05-19 16:07:34', '2014-05-19 16:07:34', '2014-05-19 16:07:34');

CREATE TABLE `verifications` (
   `verification_ident` char(37) not null,
   `verification_user_id` int(11) not null,
   `verification_type` enum('REGISTRATION','PWRESET') not null default 'REGISTRATION',
   `verification_created_at` timestamp not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
   PRIMARY KEY (`verification_user_id`,`verification_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- [Table `verifications` is empty]