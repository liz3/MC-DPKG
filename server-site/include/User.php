<?php
/**
 * Created by PhpStorm.
 * User: liz3
 * Date: 20.08.17
 * Time: 23:55
 */

namespace mc_dpkg;


class User
{
    private $email;
    private $name;
    private $password;
    private $addonsIds;

    /**
     * User constructor.
     * @param string $email
     * @param string $name
     * @param string $password
     * @param array $addonsIds
     */
    public function __construct($email, $name, $password, $addonsIds)
    {
        $this->email = $email;
        $this->name = $name;
        $this->password = $password;
        $this->addonsIds = $addonsIds;
    }


    /**
     * @param Database $mysql
     * @param int $id
     * @return User|null
     */
    static function getUserById(&$mysql, $id) {
        $result = $mysql->getById("User", $id);
        if(!$result->isSuccess()) {
            return null;
        }
        $obj = $result->getResult()->fetch_object();
        $plugins = $mysql->getSingleParameter("Plugins", "i", "USERID", $id, "`ID`");
        $pl_arr = array();
        if($plugins->isSuccess()) {
            while ($pl = $plugins->getResult()->fetch_object()) {array_push($pl_arr, $pl->ID);}
        }
        return new User($obj->EMAIL, $obj->USERNAME, $obj->PASSWORD, $pl_arr);
    }

    /**
     * @param Database $mysql
     * @param string $name
     * @return User|null
     * @internal param int $id
     */
    static function getUserByName(&$mysql, $name) {
        $result = $mysql->getSingleParameter("User", "s", "USERNAME", $name);
        if(!$result->isSuccess()) {
            return null;
        }
        $obj = $result->getResult()->fetch_object();
        $plugins = $mysql->getSingleParameter("Plugins", "i", "USERID", $obj->ID, "`ID`");
        $pl_arr = array();
        if($plugins->isSuccess()) {
            while ($pl = $plugins->getResult()->fetch_object()) {array_push($pl_arr, $pl->ID);}
        }
        return new User($obj->EMAIL, $obj->USERNAME, $obj->PASSWORD, $pl_arr);
    }

    /**
     * @param Database $mysql
     * @param string $name
     * @param string $mail
     * @param string $pass
     * @param string $pass2
     * @return array
     */
    static function createUser(&$mysql, $name, $mail, $pass, $pass2) {

        if(!Utils::checkAll($name, $mail, $pass, $pass2)) return array(false, "missing_args");


        if(strlen($pass) < 5) return array(false, "short_pass");
        if($pass2 != $pass) return array(false, "not_same_pass");

        $hash = hash("sha512", $pass.$mail);
        $result = $mysql->execute("INSERT INTO `User` (USERNAME, EMAIL, PASSWORD) VALUES (?, ?, ?)", "sss", $name, $mail, $hash);

        return array($result->isSuccess(), $result->isSuccess() ? new User($name, $mail, $hash, array()) : null);
    }
}