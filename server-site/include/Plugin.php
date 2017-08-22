<?php
/**
 * Created by PhpStorm.
 * User: liz3
 * Date: 20.08.17
 * Time: 23:53
 */

namespace mc_dpkg;


class Plugin
{
    private $id;
    private $name;
    private $versions;
    private $valid;
    private $userId;
    private $dependencies;


    /**
     * Plugin constructor.
     * @param $id
     * @param $name
     * @param $versions
     * @param $valid
     * @param $userId
     * @param $dependencies
     */
    public function __construct($id, $name, &$versions, $valid, $userId, $dependencies)
    {
        $this->id = $id;
        $this->name = $name;
        $this->versions = $versions;
        $this->valid = $valid;
        $this->userId = $userId;

        if($dependencies != null && !$dependencies == "" && !empty($dependencies)) {
            $this->dependencies = $dependencies;
        } else {
            $this->dependencies = array();
        }
    }

    /**
     * @return mixed
     */
    public function getName()
    {
        return $this->name;
    }



    /**
     * @return array
     */
    function buildForJson()
    {

        $versions = array();

        /**
         * @var PluginVersion $version
         */
        foreach ($this->versions as $version) {
            array_push($versions, array(
                "version" => $version->getVersion(),
                "hash" => $version->getHash(),
                "link" => $version->getLink(),
                "server" => $version->getServerVersions()
            ));
        }


           return array(
               "name" => $this->name,
               "versions" => $versions,
               "author" => $this->userId,
               "id" => $this->id,
               "dependencies" => $this->dependencies
           );
    }

    /**
     * @param Database $mysql
     * @param $name
     * @param bool $backports
     * @return bool|Plugin
     */
    static function getPluginByName(&$mysql, $name, $backports = false)
    {

        $result = $mysql->getSingleParameter("Plugins", "s", "NAME", $name);

        if (!$result->isSuccess()) return false;
        $plugin = $result->getResult()->fetch_object();
        $id = $plugin->ID;

        $version = $mysql->getSingleParameter("Versions", "i", "PLUGINID", $id);



        $versions = array();

        while ($ver = $version->getResult()->fetch_object()) {
            $versions[$ver->VERSION] = new PluginVersion( $ver->VERSION, $ver->SUPER_URL, $ver->HASH, explode(",", $ver->SERVERVERSION));
        }


        $pl = new Plugin($id, $plugin->NAME, $versions, $plugin->VALID == 1, $plugin->USERID, $plugin->DEPENDENCIES != null && strlen($plugin->DEPENDENCIES) != 0 ? explode(",", $plugin->DEPENDENCIES) : null);

        if (!$backports && !$pl->valid) return false;


        return $pl;
    }

    /**
     * @param Database $mysql
     * @param $name
     * @param bool $backports
     * @return bool|Plugin
     */
    static function getPluginById(&$mysql, $id, $backports = false)
    {

        $result = $mysql->getSingleParameter("Plugins", "i", "ID", $id);

        if (!$result->isSuccess()) return false;
        $plugin = $result->getResult()->fetch_object();
        $id = $plugin->ID;

        $version = $mysql->getSingleParameter("Versions", "i", "PLUGINID", $id);



        $versions = array();

        while ($ver = $version->getResult()->fetch_object()) {
            $versions[$ver->VERSION] = new PluginVersion( $ver->VERSION, $ver->SUPER_URL, $ver->HASH, explode(",", $ver->SERVERVERSION));
        }


        $pl = new Plugin($id, $plugin->NAME, $versions, $plugin->VALID == 1, $plugin->USERID, $plugin->DEPENDENCIES != null && strlen($plugin->DEPENDENCIES) != 0 ?explode(",", $plugin->DEPENDENCIES) : null);

        if (!$backports && !$pl->valid) return false;


        return $pl;
    }

    /**
     * @return mixed
     */
    public function getUserId()
    {
        return $this->userId;
    }

}

class PluginVersion
{

    private $version;
    private $link;
    private $hash;
    private $serverVersions;

    /**
     * PluginVersion constructor.
     * @param $version
     * @param $link
     * @param $hash
     * @param $serverVersions
     */
    public function __construct($version, $link, $hash, $serverVersions)
    {
        $this->version = $version;
        $this->link = $link;
        $this->hash = $hash;
        $this->serverVersions = $serverVersions;
    }


    /**
     * @return mixed
     */
    public function getVersion()
    {
        return $this->version;
    }

    /**
     * @return mixed
     */
    public function getLink()
    {
        return $this->link;
    }

    /**
     * @return mixed
     */
    public function getHash()
    {
        return $this->hash;
    }

    /**
     * @return mixed
     */
    public function getServerVersions()
    {
        return $this->serverVersions;
    }


}