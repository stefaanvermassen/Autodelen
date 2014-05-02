DROP DATABASE IF EXISTS autodelen;
CREATE DATABASE autodelen;
USE autodelen;
SET NAMES utf8;

CREATE TABLE `FileGroups` (
	`file_group_id` INT NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Files` (
  `file_id` INT(11) NOT NULL AUTO_INCREMENT,
  `file_path` VARCHAR(255) NOT NULL,
  `file_name` VARCHAR(128) NULL,
  `file_content_type` VARCHAR(64) NULL,
  `file_file_group_id` INT(11) NULL DEFAULT NULL,
  `file_created_at` DATETIME,
  `file_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`file_id`),
	FOREIGN KEY (`file_file_group_id`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;


CREATE TABLE `Addresses` (
  `address_id` INT(10) NOT NULL AUTO_INCREMENT,
  `address_country` VARCHAR(64) NOT NULL DEFAULT 'Belgium',
  `address_city` VARCHAR(64) NOT NULL,
  `address_zipcode` VARCHAR(12) NOT NULL,
  `address_street` VARCHAR(64) NOT NULL DEFAULT '0',
  `address_street_number` VARCHAR(8) NOT NULL DEFAULT '0',
  `address_street_bus` VARCHAR(4) NULL DEFAULT NULL,
  `address_created_at` DATETIME,
  `address_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  INDEX `address_place_zip` (`address_city`)
)
  COLLATE='latin1_swedish_ci'
  ENGINE=InnoDB;

CREATE TABLE `Users` (
	`user_id` INT NOT NULL AUTO_INCREMENT,
	`user_email` VARCHAR(64) NOT NULL,
	`user_password` CHAR(64) NOT NULL,
	`user_firstname` VARCHAR(64) NOT NULL,
	`user_lastname` VARCHAR(64) NOT NULL,
	`user_gender` ENUM('MALE', 'FEMALE', 'UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
	`user_cellphone` VARCHAR(16),
	`user_phone` VARCHAR(16),
	`user_address_domicile_id` INT,
	`user_address_residence_id` INT,
	`user_driver_license_id` VARCHAR(32),
	`user_driver_license_file_group_id` INT,
	`user_identity_card_id` VARCHAR(32), # Identiteitskaartnr
	`user_identity_card_registration_nr` VARCHAR(32), # Rijksregisternummer
	`user_identity_card_file_group_id` INT,
	`user_status` ENUM('EMAIL_VALIDATING','REGISTERED','FULL_VALIDATING','FULL','BLOCKED','DROPPED') NOT NULL DEFAULT 'EMAIL_VALIDATING', # Stadia die de gebruiker moet doorlopen
	`user_damage_history` TEXT,
	`user_payed_deposit` BIT(1),
	`user_agree_terms` BIT(1),
	`user_contract_manager_id` INT,
	`user_image_id` INT,
	`user_created_at` DATETIME,
	`user_last_notified` DATETIME,
	`user_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`user_id`),
	FOREIGN KEY (`user_address_domicile_id`) REFERENCES Addresses(`address_id`),
	FOREIGN KEY (`user_address_residence_id`) REFERENCES Addresses(`address_id`),
	FOREIGN KEY (`user_driver_license_file_group_id`) REFERENCES FileGroups(`file_group_id`),
	FOREIGN KEY (`user_identity_card_file_group_id`) REFERENCES FileGroups(`file_group_id`),
	FOREIGN KEY (`user_contract_manager_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`user_image_id`) REFERENCES Files(`file_id`),
	UNIQUE INDEX `user_email` (`user_email`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `UserRoles` (
	`userrole_userid` INT NOT NULL,
	`userrole_role` ENUM('SUPER_USER', 'CAR_OWNER', 'CAR_USER', 'INFOSESSION_ADMIN', 'MAIL_ADMIN', 'PROFILE_ADMIN', 'RESERVATION_ADMIN', 'CAR_ADMIN') NOT NULL,
	PRIMARY KEY (`userrole_userid`, `userrole_role`),
	FOREIGN KEY (`userrole_userid`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarInsurances` (
	`insurance_id` INT NOT NULL AUTO_INCREMENT,
	`insurance_name` VARCHAR(64),
	`insurance_expiration` DATE,
	`insurance_contract_id` INT, # Polisnr
	`insurance_bonus_malus` INT,
	`insurance_created_at` DATETIME,
	`insurance_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`insurance_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `TechnicalCarDetails` (
	`details_id` INT NOT NULL AUTO_INCREMENT,
	`details_car_license_plate` VARCHAR(64),
	`details_car_registration` INT,
	`details_car_chassis_number` INT(17),
	`details_created_at` DATETIME,
	`details_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`details_id`),
	UNIQUE INDEX `ix_details` (`details_car_license_plate`, `details_car_chassis_number`),
	FOREIGN KEY (`details_car_registration`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;


CREATE TABLE `Cars` (
	`car_id` INT NOT NULL AUTO_INCREMENT,
	`car_name` VARCHAR(64) NOT NULL,
	`car_type` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_brand` VARCHAR(64) NOT NULL DEFAULT '0',
	`car_location` INT,
	`car_seats` TINYINT UNSIGNED,
	`car_doors` TINYINT UNSIGNED,
	`car_year` INT,
	`car_gps` BIT(1),
	`car_hook` BIT(1),
	`car_fuel` ENUM('PETROL','DIESEL', 'BIODIESEL', 'GAS', 'HYBRID', 'ELECTRIC'),
	`car_fuel_economy` INT,
	`car_estimated_value` INT,
	`car_owner_annual_km` INT,
	`car_technical_details` INT,
	`car_insurance` INT,
	`car_owner_user_id` INT NOT NULL,
	`car_comments` VARCHAR(256),
	`car_images_id` INT,
	`car_created_at` DATETIME,
	`car_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_id`),
	FOREIGN KEY (`car_owner_user_id`) REFERENCES Users(`user_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_location`) REFERENCES Addresses(`address_id`) ON DELETE CASCADE,
	FOREIGN KEY (`car_images_id`) REFERENCES FileGroups(`file_group_id`),
	FOREIGN KEY (`car_technical_details`) REFERENCES TechnicalCarDetails(`details_id`),
	FOREIGN KEY (`car_insurance`) REFERENCES CarInsurances(`insurance_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarReservations` (
	`reservation_id` INT NOT NULL AUTO_INCREMENT,
	`reservation_status` ENUM('REQUEST','ACCEPTED', 'REFUSED', 'CANCELLED', 'REQUEST_DETAILS', 'DETAILS_PROVIDED', 'FINISHED') NOT NULL DEFAULT 'REQUEST', # Reeds goedgekeurd?
	`reservation_car_id` INT NOT NULL,
	`reservation_user_id` INT NOT NULL,
	`reservation_from` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`reservation_to` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`reservation_created_at` DATETIME,
	`reservation_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`reservation_id`),
	FOREIGN KEY (`reservation_car_id`) REFERENCES Cars(`car_id`), # Wat moet er gebeuren als de auto verwijderd wordt?
	FOREIGN KEY (`reservation_user_id`) REFERENCES Users(`user_id`) ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `InfoSessions` (
	`infosession_id` INT NOT NULL AUTO_INCREMENT,
	`infosession_type` ENUM('NORMAL', 'OWNER', 'OTHER') NOT NULL DEFAULT 'NORMAL',
	`infosession_type_alternative` VARCHAR(64),
	`infosession_timestamp` TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00',
	`infosession_address_id` INT NOT NULL,
	`infosession_host_user_id` INT,
	`infosession_max_enrollees` INT,
	`infosession_comments` VARCHAR(256),
	`infosession_created_at` DATETIME,
	`infosession_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`infosession_id`),
	FOREIGN KEY (`infosession_host_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`infosession_address_id`) REFERENCES Addresses(`address_id`)	
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `InfoSessionEnrollees` ( # Wie is ingeschreven?
	`infosession_id` INT NOT NULL,
	`infosession_enrollee_id` INT NOT NULL,
	`infosession_enrollment_status` ENUM('ENROLLED', 'PRESENT', 'ABSENT') NOT NULL DEFAULT 'ENROLLED',
	PRIMARY KEY (`infosession_id`, `infosession_enrollee_id`),
	FOREIGN KEY (`infosession_enrollee_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`infosession_id`) REFERENCES InfoSessions(`infosession_id`) ON DELETE CASCADE
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarAvailabilities` (
	`car_availability_id` INT NOT NULL AUTO_INCREMENT,
	`car_availability_car_id` INT NOT NULL,
	`car_availability_begin_day_of_week` INT NOT NULL,
	`car_availability_begin_time` TIME NOT NULL,
	`car_availability_end_day_of_week` INT NOT NULL,
	`car_availability_end_time` TIME NOT NULL,
	PRIMARY KEY (`car_availability_id`),
	FOREIGN KEY (`car_availability_car_id`) REFERENCES Cars(`car_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarPrivileges` (
	`car_privilege_user_id` INT NOT NULL,
	`car_privilege_car_id` INT NOT NULL,
	PRIMARY KEY (`car_privilege_user_id`,`car_privilege_car_id`),
	FOREIGN KEY (`car_privilege_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`car_privilege_car_id`) REFERENCES Cars(`car_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarCosts` (
	`car_cost_id` INT NOT NULL AUTO_INCREMENT,
	`car_cost_car_id` INT NOT NULL,
	`car_cost_proof` INT,
	`car_cost_amount` DECIMAL(19,4) NOT NULL,
	`car_cost_description` TEXT,
	`car_cost_status` ENUM('REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'REQUEST', #approved by car_admin
	`car_cost_time` DATETIME,
	`car_cost_mileage` DECIMAL(10,1),
	`car_cost_created_at` DATETIME,
	`car_cost_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`car_cost_id`),
	FOREIGN KEY (`car_cost_car_id`) REFERENCES Cars(`car_id`),
	FOREIGN KEY (`car_cost_proof`) REFERENCES Files(`file_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `CarRides` (
  `car_ride_car_reservation_id` INT NOT NULL, # also primary key
  `car_ride_status` BIT(1) NOT NULL DEFAULT 0, # approved by owner?
  `car_ride_start_mileage` DECIMAL(10,1),
  `car_ride_end_mileage` DECIMAL(10,1),
  `car_ride_damage` BIT(1) NOT NULL DEFAULT 0,
  `car_ride_refueling` INT NOT NULL,
  `car_ride_created_at` DATETIME,
  `car_ride_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`car_ride_car_reservation_id`),
  FOREIGN KEY (`car_ride_car_reservation_id`) REFERENCES CarReservations(`reservation_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Refuels` (
	`refuel_id` INT NOT NULL AUTO_INCREMENT,
	`refuel_car_ride_id` INT NOT NULL,
	`refuel_file_id` INT,
	`refuel_amount` DECIMAL(19,4),
	`refuel_status` ENUM('CREATED', 'REQUEST','ACCEPTED', 'REFUSED') NOT NULL DEFAULT 'CREATED', #approved by owner
   	`refuel_created_at` DATETIME,
   	`refuel_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`refuel_id`),
	FOREIGN KEY (`refuel_car_ride_id`) REFERENCES CarRides(`car_ride_car_reservation_id`),
	FOREIGN KEY (`refuel_file_id`) REFERENCES Files(`file_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Damages` (
	`damage_id` INT NOT NULL AUTO_INCREMENT,
	`damage_car_ride_id` INT NOT NULL,
	`damage_filegroup_id` INT NOT NULL,
	`damage_description` TEXT NOT NULL,
   	`damage_created_at` DATETIME,
   	`damage_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`damage_id`),
	FOREIGN KEY (`damage_car_ride_id`) REFERENCES CarRides(`car_ride_car_reservation_id`),
	FOREIGN KEY (`damage_filegroup_id`) REFERENCES FileGroups(`file_group_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Messages` ( # from user to user != Notifications
	`message_id` INT NOT NULL AUTO_INCREMENT,
	`message_from_user_id` INT NOT NULL,
	`message_to_user_id` INT NOT NULL,
	`message_read` BIT(1) NOT NULL DEFAULT 0,
	`message_subject` VARCHAR(255) NOT NULL DEFAULT 'Bericht van een Dégage-gebruiker',
	`message_body` TEXT NOT NULL,
   `message_created_at` DATETIME,
   `message_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`message_id`),
	FOREIGN KEY (`message_from_user_id`) REFERENCES Users(`user_id`),
	FOREIGN KEY (`message_to_user_id`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Templates` (
	`template_id` INT NOT NULL,
	`template_title` VARCHAR(255) NOT NULL,
	`template_subject` VARCHAR(255) NOT NULL DEFAULT 'Bericht van Dégage!',
	`template_body` TEXT NOT NULL,
	`template_send_mail` BIT(1) NOT NULL DEFAULT 1, # Mail of notificatie verzenden? Instelbaar via dashboard mailtemplates
	`template_send_mail_changeable` BIT(1) NOT NULL DEFAULT 1, # Mag aangepast worden? Bv wachtwoord reset/verificatie niet!
	`template_created_at` DATETIME,
	`template_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`template_id`),
	UNIQUE INDEX `template_title` (`template_title`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `TemplateTags` ( # Welke tags kunnen we gebruiken in de templates
	`template_tag_id` INT NOT NULL AUTO_INCREMENT,
	`template_tag_body` VARCHAR(255) NOT NULL,
	PRIMARY KEY (`template_tag_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `TemplateTagAssociations` ( # Welke tags horen bij welke templates
	`template_tag_association_id` INT NOT NULL AUTO_INCREMENT,
	`template_tag_id` INT NOT NULL,
	`template_id` INT NOT NULL,
	PRIMARY KEY (`template_tag_association_id`),
	FOREIGN KEY (`template_id`) REFERENCES Templates(`template_id`),
	FOREIGN KEY (`template_tag_id`) REFERENCES TemplateTags(`template_tag_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Verifications` (
	`verification_ident` CHAR(37) NOT NULL,
	`verification_user_id` INT(11) NOT NULL,
	`verification_type` ENUM('REGISTRATION','PWRESET') NOT NULL DEFAULT 'REGISTRATION',
	`verification_created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`verification_user_id`, `verification_type`),
	CONSTRAINT `FK_VERIFICATION_USER` FOREIGN KEY (`verification_user_id`) REFERENCES `Users` (`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `Notifications` ( # from system to user
	`notification_id` INT NOT NULL AUTO_INCREMENT,
	`notification_user_id` INT NOT NULL,
	`notification_read` BIT(1) NOT NULL DEFAULT 0,
	`notification_subject` VARCHAR(255),
	`notification_body` TEXT,
   `notification_created_at` DATETIME,
   `notification_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`notification_id`),
	FOREIGN KEY (`notification_user_id`) REFERENCES Users(`user_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

CREATE TABLE `approvals` (
  `approval_id` INT(11) NOT NULL AUTO_INCREMENT,
  `approval_user` INT(11) NULL DEFAULT NULL,
  `approval_admin` INT(11) NULL DEFAULT NULL,
  `approval_submission` DATETIME NOT NULL,
  `approval_date` DATETIME NULL DEFAULT NULL,
  `approval_status` ENUM('PENDING','ACCEPTED','DENIED') NOT NULL DEFAULT 'PENDING',
  `approval_infosession` INT(11) NULL DEFAULT NULL,
  `approval_user_message` TEXT NULL,
  `approval_admin_message` TEXT NULL,
  PRIMARY KEY (`approval_id`),
  INDEX `FK_approval_user` (`approval_user`),
  INDEX `FK_approval_admin` (`approval_admin`),
  INDEX `FK_approval_session` (`approval_infosession`),
  CONSTRAINT `FK_approval_admin` FOREIGN KEY (`approval_admin`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FK_approval_session` FOREIGN KEY (`approval_infosession`) REFERENCES `infosessions` (`infosession_id`),
  CONSTRAINT `FK_approval_user` FOREIGN KEY (`approval_user`) REFERENCES `users` (`user_id`)
)
  COLLATE='latin1_swedish_ci'
  ENGINE=InnoDB;

CREATE TABLE `settings` (
  `setting_id` INT(11) NOT NULL AUTO_INCREMENT,
  `setting_name` CHAR(32) NOT NULL,
  `setting_value` VARCHAR(256) NULL DEFAULT NULL,
  `setting_after` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`setting_id`)
)
  ENGINE=InnoDB;

DELIMITER $$

CREATE TRIGGER Files_ins BEFORE INSERT ON Files FOR EACH ROW
BEGIN
  IF new.file_created_at IS NULL THEN
    SET new.file_created_at = now();
  END IF;
END $$

CREATE TRIGGER Addresses_ins BEFORE INSERT ON Addresses FOR EACH ROW
BEGIN
  IF new.address_created_at IS NULL THEN
    SET new.address_created_at = now();
  END IF;
END $$

CREATE TRIGGER Users_ins BEFORE INSERT ON Users FOR EACH ROW
BEGIN
  IF new.user_created_at IS NULL THEN
    SET new.user_created_at = now();
  END IF;
  IF new.user_last_notified IS NULL THEN
    SET new.user_last_notified = now();
  END IF;
END $$

CREATE TRIGGER Cars_ins BEFORE INSERT ON Cars FOR EACH ROW
BEGIN
  IF new.car_created_at IS NULL THEN
    SET new.car_created_at = now();
  END IF;
END $$

CREATE TRIGGER CarInsurances_ins BEFORE INSERT ON CarInsurances FOR EACH ROW
BEGIN
  IF new.insurance_created_at IS NULL THEN
    SET new.insurance_created_at = now();
  END IF;
END $$

CREATE TRIGGER TechnicalCarsDetails_ins BEFORE INSERT ON TechnicalCarDetails FOR EACH ROW
BEGIN
  IF new.details_created_at IS NULL THEN
    SET new.details_created_at = now();
  END IF;
END $$

CREATE TRIGGER CarReservations_ins BEFORE INSERT ON CarReservations FOR EACH ROW
BEGIN
  IF new.reservation_created_at IS NULL THEN
    SET new.reservation_created_at = now();
  END IF;
END $$

CREATE TRIGGER InfoSessions_ins BEFORE INSERT ON InfoSessions FOR EACH ROW
BEGIN
  IF new.infosession_created_at IS NULL THEN
    SET new.infosession_created_at = now();
  END IF;
END $$

CREATE TRIGGER CarCosts_ins BEFORE INSERT ON CarCosts FOR EACH ROW
BEGIN
  IF new.car_cost_created_at IS NULL THEN
    SET new.car_cost_created_at = now();
  END IF;
END $$

CREATE TRIGGER Refuels_ins BEFORE INSERT ON Refuels FOR EACH ROW
BEGIN
  IF new.refuel_created_at IS NULL THEN
    SET new.refuel_created_at = now();
  END IF;
END $$

CREATE TRIGGER Damages_ins BEFORE INSERT ON Damages FOR EACH ROW
BEGIN
  IF new.damage_created_at IS NULL THEN
    SET new.damage_created_at = now();
  END IF;
END $$

CREATE TRIGGER CarRides_ins BEFORE INSERT ON CarRides FOR EACH ROW
BEGIN
  IF new.car_ride_created_at IS NULL THEN
    SET new.car_ride_created_at = now();
  END IF;
END $$

CREATE TRIGGER Templates_ins BEFORE INSERT ON Templates FOR EACH ROW
BEGIN
  IF new.template_created_at IS NULL THEN
    SET new.template_created_at = now();
  END IF;
END $$

CREATE TRIGGER Messages_ins BEFORE INSERT ON Messages FOR EACH ROW
BEGIN
  IF new.message_created_at IS NULL THEN
    SET new.message_created_at = now();
  END IF;
END $$

CREATE TRIGGER Notifications_ins BEFORE INSERT ON Notifications FOR EACH ROW
BEGIN
  IF new.notification_created_at IS NULL THEN
    SET new.notification_created_at = now();
  END IF;
END $$
DELIMITER ;