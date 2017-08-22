<?php
/**
 * Created by PhpStorm.
 * User: liz3
 * Date: 21.08.17
 * Time: 00:14
 */

namespace mc_dpkg;


class Utils
{
    /**
     * @param string $haystack
     * @param string $needle
     * @return bool
     */
    static function startsWith($haystack, $needle) {
        $length = strlen($needle);
        return (substr($haystack, 0, $length) === $needle);
    }

    /**
     * @param string $haystack
     * @param string $needle
     * @return bool
     */
    static function endsWith($haystack, $needle) {
        $length = strlen($needle);

        if($length == 0) return true;

        return (substr($haystack, -$length) === $needle);
    }

    /**
     * @param $var
     * @return bool
     */
    static function checkVar($var) {
        return isset($var) && !empty($var);
    }

    /**
     * @param array ...$vars
     * @return bool
     */
    static function checkAll(...$vars) {

        foreach ($vars as $var) {
            if(!self::checkVar($var)) return false;
        }

        return true;
    }

}