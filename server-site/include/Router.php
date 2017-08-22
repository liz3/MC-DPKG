<?php
/**
 * Created by PhpStorm.
 * User: liz3
 * Date: 20.08.17
 * Time: 23:40
 */

namespace mc_dpkg;


class Router
{
    private $success;
    private $action;
    private $arguments;
    private $responseBody;
    private $responseHeaders;

    public function __construct()
    {

        $this->responseHeaders = array();
        $this->arguments = array();
        $this->success = true;
        $this->handle();

    }
    private function handle() {

        if($_SERVER["REQUEST_METHOD"] != "POST") {
            $this->templateErrorResponse("unsupported_method");
        }

        $this->arguments = json_decode(file_get_contents("php://input"), true);
        $this->action = $this->arguments["action"];
        if($this->action == null) $this->templateErrorResponse("empty_action");



    }

    private function preWork() {
        foreach ($this->responseHeaders as $responseHeader) {
            header($responseHeader);
        }
    }

    function templateErrorResponse($data) {
        $this->success = false;
        $this->responseBody = $data;
        $this->responseJson();
    }
     function responseJson() {

        $this->preWork();
        header("Content-type: application/json");

        die(json_encode(array(
            "success" => $this->success,
            "data" => $this->responseBody,
            "time" => time()
        )));

    }

    /**
     * @return bool
     */
    public function getSuccess()
    {
        return $this->success;
    }

    /**
     * @param bool $success
     */
    public function setSuccess($success)
    {
        $this->success = $success;
    }

    /**
     * @return mixed
     */
    public function getResponseBody()
    {
        return $this->responseBody;
    }

    /**
     * @param mixed $responseBody
     */
    public function setResponseBody($responseBody)
    {
        $this->responseBody = $responseBody;
    }

    /**
     * @return mixed
     */
    public function getAction()
    {
        return $this->action;
    }

    /**
     * @return mixed
     */
    public function getArguments()
    {
        return $this->arguments;
    }


    /**
     * @return array
     */
    public function getResponseHeaders()
    {
        return $this->responseHeaders;
    }

}