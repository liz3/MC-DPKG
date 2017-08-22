<?php
/**
 * Created by PhpStorm.
 * User: liz3
 * Date: 20.08.17
 * Time: 22:59
 */

namespace mc_dpkg;


class Database
{
    private $connection;
    private $failed;
    private $error;


    function __construct()
    {
        $this->connection = new \mysqli(MYSQL_HOST, MYSQL_USER, MYSQL_PASS, MYSQL_DATABASE, MYSQL_PORT);
        $this->failed = $this->connection->error != null;
        if(!$this->failed) $this->createTables();
    }
    function get_errors() {
        if(!$this->failed) return false;
        return $this->connection->error;
    }

    private function createTables() {

        $statements = array(
            "CREATE TABLE IF NOT EXISTS `Plugins` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `NAME` VARCHAR(250), `USERID` INT(11),`VALID` INT(11), `CATEGORY` VARCHAR (250),`DEPENDENCIES` TEXT,PRIMARY KEY(`ID`))",
            "CREATE TABLE IF NOT EXISTS `User` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `USERNAME` VARCHAR(25), `EMAIL` VARCHAR(250), `PASSWORD` VARCHAR(520), PRIMARY KEY (`ID`))",
            "CREATE TABLE IF NOT EXISTS `Versions` (`ID` INT(11) NOT NULL AUTO_INCREMENT, `PLUGINID` INT(11), `SUPER_URL` TEXT, `VERSION` VARCHAR(25), `SERVERVERSION` TEXT, `HASH` VARCHAR(520), PRIMARY KEY (`ID`))"
        );

        foreach ($statements as $statement) {
            if(!$this->connection->query($statement)) {
                $this->error = $this->connection->error;
                $this->failed = true;
                die($this->connection->error);
                return;
            }
        }
    }

    /**
     * @param string $table
     * @param int $id
     * @return bool|MysqlQuery
     */
    function getById($table, $id) {

        return $this->execute("SELECT * FROM `$table` WHERE `ID` = (?)", "i", $id);
    }

    /**
     * @param string $table
     * @param string $type
     * @param mixed $what
     * @param mixed $value
     * @param string $fetch
     * @return bool|MysqlQuery
     */
    function getSingleParameter($table, $type, $what, $value, $fetch = "*") {

        return $this->execute("SELECT $fetch FROM `$table` WHERE `$what` = (?)", $type, $value);
    }

    function execute($statement, $keys, ...$values) {
        if($this->failed) return false;

        $arr = array();
        array_push($arr, $keys);
        foreach ($values as $value) {
            array_push($arr, $value);
        }
        $tmp = array();
        foreach ($arr as $key => $value) {
            $tmp[$key] = &$arr[$key];
        }
        $prepared = $this->connection->prepare($statement);
        call_user_func_array(array($prepared, "bind_param"), $tmp);
        $success = $prepared->execute();

        return new MysqlQuery($success, $prepared);

    }
}
class MysqlQuery {

    private $prepared;
    private $success;
    private $insertId;
    private $result;
    private $error;

    /**
     * MysqlQuery constructor.
     * @param bool $success
     * @param \mysqli_stmt $prepared
     */
    function __construct($success, &$prepared)
    {
        $this->success = $success;
        $this->prepared = $prepared;
        if($this->success) {
            $this->insertId = $prepared->insert_id;
            $this->result = $prepared->get_result();
        } else {
           $this->error = $prepared->error;
        }


    }

    /**
     * @return \mysqli_stmt
     */
    public function getPrepared()
    {
        return $this->prepared;
    }

    /**
     * @return bool
     */
    public function isSuccess()
    {
        return $this->success;
    }

    /**
     * @return int
     */
    public function getInsertId()
    {
        return $this->insertId;
    }

    /**
     * @return bool|\mysqli_result
     */
    public function getResult()
    {
        return $this->result;
    }

    /**
     * @return string
     */
    public function getError()
    {
        return $this->error;
    }


}