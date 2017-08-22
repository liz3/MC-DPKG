<?php


require_once __DIR__."/auto_load.php";

$database = new \mc_dpkg\Database();
$router = new \mc_dpkg\Router();



$action = $router->getAction();
if($action == "search") {




    $backports = isset($router->getArguments()["backports"]) &&
        $router->getArguments()["backports"] == true;

    $found = array();

   if(isset($router->getArguments()["packages"])) {
       $packages = $router->getArguments()["packages"];
       foreach ($packages as $package) {
           $pl =  \mc_dpkg\Plugin::getPluginByName($database, $package, $backports);

           if($pl !== false) {
               $found[$package] = $pl;
           } else {
               $found[$package] = false;

           }
       }

   }
    if(isset($router->getArguments()["ids"])) {

        $ids = $router->getArguments()["ids"];
        foreach ($ids as $package) {
            $pl =  \mc_dpkg\Plugin::getPluginById($database, $package, $backports);

            if($pl !== false) {
                $found[$pl->getName()] = $pl;
            } else {
                $found[$pl->getName()] = false;

            }
        }
    }



    $toBuild = array();

    foreach ($found as $key => $value) {
        if($value == false) {
            $toBuild[$key] = false;
            continue;
        }
        $toBuild[$key] = $value->buildForJson();
    }
    $router->setResponseBody($toBuild);
    $router->responseJson();
}