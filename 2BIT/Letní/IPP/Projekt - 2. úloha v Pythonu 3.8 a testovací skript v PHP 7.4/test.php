<?php

/**
 * Trida pro definici vyctovych hodnot navratovych kodu.
 */
class ErrorCodes
{
    /* Činnost skriptu bez chyb, */
    public const Success = 0;

    /* chybějící parametr skriptu (je-li třeba) nebo použití zakázané kombinace parametrů */
    public const InvalidParameters = 10;

    /* zadaný adresář (path v parametru --directory) nebo zadaný soubor (file v parametrech--parse-script, --int-script, --jexamxml a --jexamcfg) neexistuje či není přístupný. */
    public const FileOrDirectoryNotAccesable = 41;
}

class Arguments
{
    public $help = null;
    /* Jeli přítomen tak se prochází zvolený adresář rekurzivně */
    public $recursive = null;
    /* Tento parametr se nesmí kombinovat s parametry --int-only a --int-script */
    public $parse_only = null;
    /* Ttento parametr se nesmí kombinovat s parametry --parse-only a --parse-script */
    public $int_only = null;
    /* Chybí-li tak implicitně procházet aktuální adresář */
    public $directory = ".";
    /* Chybí-li tak implicitně použít parse_script z aktuálního adresáře */
    public $parse_script = "parse.php";
    /* Chybí-li tak implicitně použít int_script z aktuálního adresáře */
    public $int_script = "interpret.py";

    public $jexamxml = "/pub/courses/ipp/jexamxml/jexamxml.jar";
    public $jexamcfg = "/pub/courses/ipp/jexamxml/options";

    public static $longopts = array(
        "help",
        "recursive",
        "parse-only",
        "int-only",
        "directory:",
        "parse-script:",
        "int-script:",
        "jexamxml:",
        "jexamcfg:",
    );
    /* Musíme zkontrolovat jestli je uvedená cesta validní */
    function setParseScript($path)
    {
        $this->isPathValid($path);
        $this->parse_script = $path;
    }
    function setIntScript($path)
    {
        $this->isPathValid($path);
        $this->int_script = $path;
    }
    function setDirectory($dir)
    {
        $this->isPathValid($dir);
        $this->directory = $dir;
    }
    function setJavaXML($path)
    {
        $this->isPathValid($path);
        $this->jexamxml = $path;
    }
    function setJavaXMLConfig($path)
    {
        $this->isPathValid($path);
        $this->jexamcfg = $path;
    }
    function isPathValid($path)
    {
        if(!file_exists($path))
            exit(ErrorCodes::FileOrDirectoryNotAccesable);

    }
}
function parseArgs()
{
    global $argc;
    $arguments = new Arguments();
    $options = getopt("", Arguments::$longopts);
    if(key_exists("help", $options))
    {
        $arguments->help = true;
        if($argc-1 > 1)
        {
            exit(ErrorCodes::InvalidParameters);       
        }
    }

    $arguments->recursive = key_exists("recursive", $options);
    $arguments->parse_only = key_exists("parse-only", $options);
    $arguments->int_only = key_exists("int-only", $options);

    if(key_exists("parse-script", $options))
        $arguments->setParseScript($options["parse-script"]);
    if(key_exists("int-script", $options))
        $arguments->setIntScript($options["int-script"]);
    if(key_exists("directory", $options))
        $arguments->setDirectory($options["directory"]);
    if(key_exists("jexamxml", $options))
        $arguments->setJavaXML($options["jexamxml"]);
    if(key_exists("jexamcfg", $options))
        $arguments->setJavaXMLConfig($options["jexamcfg"]);

    /* Kontrola zakázaných kombinací parametrů */
    if($arguments->int_only && $arguments->parse_only)
        exit(ErrorCodes::InvalidParameters);
    if($arguments->int_only && key_exists("parse-script", $options))
        exit(ErrorCodes::InvalidParameters);
    if($arguments->parse_only && key_exists("int-script", $options))
        exit(ErrorCodes::InvalidParameters);

    /* Kontrola neznámého přepínače */
    if(count($options) != $argc - 1)
        exit(ErrorCodes::InvalidParameters);

    return $arguments;
}
class PageGenerator 
{
    static function GeneratePage($testResults, $testTime)
    {
        $succesfulTests = 0;
        $totalNumberOfTests = count($testResults);
        foreach($testResults as $key=>$testItem)
        {
            if($testItem->wasSuccesful())
                $succesfulTests++;
        }
        $wrongTests = $totalNumberOfTests - $succesfulTests;
        $perecntageOfSucces = round($succesfulTests/($totalNumberOfTests/100), 2);
        $testTime = round($testTime, 2);
        $page = file_get_contents('other/test_page.html');
        $page .= "\t<div class='header'>";
        $page .= "\t\t<h1>IPP 2021 - test.php</h1>";
        $page .= "\t\t<p>Souhrn výsledků automatického testování</p>";
        $page .= "\t\t<p>Celkem testů: $totalNumberOfTests</p>";
        $page .= "\t\t<p>Úspěšných testů: $succesfulTests</p>";
        $page .= "\t\t<p>Neúspěšných testů: $wrongTests</p>";
        $page .= "\t\t<p>Celková úspěšnost: $perecntageOfSucces%</p>";
        $page .= "\t\t<p>Testování trvalo: $testTime sekund</p>";
        $page .= "\t</div>";
        $page .= "\t<div class='flex-container'>";
        foreach($testResults as $key=>$testItem)
        {
            $testResultClass = $testItem->wasSuccesful() ? "test-good" : "test-wrong";
            $testName = $testItem->getName();
            $testDirectory = $testItem->getDirectory();
            $errorMsg = $testItem->doReturnCodesMatch() ? "" : "Chybný návratový kód. Očekáváno: ".$testItem->getExpectedReturnCode()." Obdrženo: ".$testItem->getReceivedReturnCode();
            $errorMsg .= $testItem->doOutputsMatch() ? "" : " Rozdílný výstup";
            $page .= PageGenerator::CreateDiv($testResultClass, $testDirectory, $testName, $errorMsg);
        }
        $page .= "\t</div>".PHP_EOL;
        $page .= "</body>".PHP_EOL;
        $page .= "</html>".PHP_EOL;
        return $page;
    }

    static function CreateDiv($classes, $testDiretory, $testName, $error)
    {
        $div = "\t<div class='test-item ".$classes."'>".PHP_EOL;
        $div .= "\t\t<div class='test-directory'><a href='$testDiretory'>".$testDiretory."</a></div>".PHP_EOL;
        $div .= "\t\t<div class='test-name'>$testName</div>".PHP_EOL;
        $div .= "\t\t<div class='test-error'>$error</div>".PHP_EOL;
        $div .= "\t</div>".PHP_EOL;
        return $div;
    }
}
class Tester
{
    public static function runTests($testItemsToBeTested, $config)
    {
        if($config->parse_only || $config->int_only)
        {
            // Použít A7Soft JExamXML
            foreach($testItemsToBeTested as $key=>$testItem)
            {
                $output=null;
                $returnValue=null;
                $lol = $testItem->getSourceFile();
                //fwrite(STDERR,"Prave testuji $lol\n");
                if($config->parse_only)
                    exec('php7.4  '. $config->parse_script . ' < '.$testItem->getSourceFile(), $output, $returnValue);
                else
                    exec('python3.8 '. $config->int_script . ' --source='.$testItem->getSourceFile() . ' --input='.$testItem->getInputFile(), $output, $returnValue);
                $testItem->setReceivedReturnCode($returnValue);
            
                if($testItem->shouldOutputsBeCompared() == true)
                {
                    //Vytvořit dočasný soubor pokud nexistuje 
                    $referenceOutputFile = $testItem->getOutputFile();
                    $tmpFilePath = $referenceOutputFile."_tmp";
                    $myfile = fopen($tmpFilePath, "w") or die("Unable to open file!");
                    fwrite($myfile, implode(PHP_EOL, $output));
                    fclose($myfile);

                    //Porovnat soubory 
                    $comaper = "";
                    if($config->parse_only)
                        $comaper = "java -jar $config->jexamxml $referenceOutputFile $tmpFilePath $config->jexamxmlConfig";
                    else
                        $comaper = "diff ".$tmpFilePath." ".$referenceOutputFile;
                    exec($comaper, $output, $retCode);
                    $testItem->setOutputsMatch($retCode == 0);
                    //unlink($tmpFilePath);
                }
            }
        }
        else
        {
            foreach($testItemsToBeTested as $key=>$testItem)
            {
                $parse_output =null;
                $interpret_output = null;
                $parse_return_value = null;
                $interpret_return_value = null;
                $lol = $testItem->getSourceFile();
                //fwrite(STDERR,"Prave testuji $lol\n");
                exec('php7.4 '. $config->parse_script . ' < '.$testItem->getSourceFile(), $parse_output, $parse_return_value);
                //fwrite(STDERR,"Parseing done\n");
                if($parse_return_value != 0)
                {
                    $testItem->setReceivedReturnCode($parse_return_value);
                    continue;
                }
                $referenceOutputFile = $testItem->getOutputFile();
                $tmpFilePath = $referenceOutputFile."_tmp";
                $myfile = fopen($tmpFilePath, "w") or die("Unable to open file!");
                fwrite($myfile, implode(PHP_EOL, $parse_output));
                fclose($myfile);
                exec('python3.8 '. $config->int_script . ' --source='.$tmpFilePath. ' --input='.$testItem->getInputFile(), $interpret_output, $interpret_return_value);
                //unlink($tmpFilePath);
                $testItem->setReceivedReturnCode($interpret_return_value);

                if($testItem->shouldOutputsBeCompared() == true)
                {
                    //Vytvořit dočasný soubor pokud nexistuje 
                    $referenceOutputFile = $testItem->getOutputFile();
                    $tmpFilePath = $referenceOutputFile."_tmp";
                    $myfile = fopen($tmpFilePath, "w") or die("Unable to open file!");
                    fwrite($myfile, implode(PHP_EOL, $interpret_output));
                    fclose($myfile);

                    //Porovnat soubory 
                    $comaper = "diff ".$tmpFilePath." ".$referenceOutputFile;
                    $cmp_return_code = null;
                    exec($comaper, $output, $cmp_return_code);
                    $testItem->setOutputsMatch($cmp_return_code == 0);
                    //unlink($tmpFilePath);
                }
                //fwrite(STDERR,"Interpret done\n");
            }
        }
        return $testItemsToBeTested;
    }
}
class TestItem 
{
    private $directory;
    private $name;

    private $sourceFilePath;
    private $inputFilePath;
    private $outputFilePath;
    private $returnCodeFilePath;

    private $expectedRetrunCode;
    private $receivedReturnCode;
    private $doReturnCodesMatch;

    private $outPutsMatch = true;

    public function __construct($directory, $name) 
    {
        $this->directory = $directory.DIRECTORY_SEPARATOR;
        $this->name = $name;
        $this->sourceFilePath = $this->directory.$this->name.".src";
        $this->inputFilePath = $this->directory.$this->name.".in";
        $this->outputFilePath = $this->directory.$this->name.".out";
        $this->returnCodeFilePath = $this->directory.$this->name.".rc";
        $this->checkIfFileExists($this->inputFilePath, "");
        $this->checkIfFileExists($this->outputFilePath, "");
        $this->checkIfFileExists($this->returnCodeFilePath, "0");

        $myfile = fopen($this->returnCodeFilePath, "r") or die("Unable to open file!");
        $this->expectedRetrunCode = fgets($myfile);
        fclose($myfile);
    }
    private function checkIfFileExists($directory, $input)
    {
        if(!file_exists($directory))
        {
            $myfile = fopen($directory, "w") or die("Unable to open file!");
            fwrite($myfile, $input);
            fclose($myfile);
        }
    }
    public function getSourceFile()
    {
        return $this->sourceFilePath;
    }
    public function getInputFile()
    {
        return $this->inputFilePath;
    }
    public function getOutputFile()
    {
        return $this->outputFilePath;
    }
    public function getRetrunCodeFile()
    {
        return $this->returnCodeFilePath;
    }
    public function getExpectedReturnCode()
    {
        return $this->expectedRetrunCode;
    }
    public function getReceivedReturnCode()
    {
        return $this->receivedReturnCode;
    }
    public function setReceivedReturnCode($receivedReturnCode)
    {
        $this->receivedReturnCode = $receivedReturnCode;
        $this->doReturnCodesMatch = $this->expectedRetrunCode == $this->receivedReturnCode;
    }
    public function doReturnCodesMatch()
    {
        return $this->doReturnCodesMatch;
    }
    public function shouldOutputsBeCompared()
    {
        return $this->expectedRetrunCode == 0;
    }
    public function doOutputsMatch()
    {
        return $this->outPutsMatch;
    }
    public function setOutputsMatch($match)
    {
        $this->outPutsMatch = $match;
    }
    public function getName()
    {
        return $this->name;
    }
    public function getDirectory()
    {
        return $this->directory;
    }
    public function wasSuccesful()
    {
        if (($this->outPutsMatch == true) && ($this->doReturnCodesMatch == true))
            return true;
        return false;
    }
}
class TestsLoader
{
    public static function getTests($config)
    {
        $files = self::scanDirectory($config->recursive, $config->directory);
        return self::findTests($files);
    }
    private function findTests($files)
    {
        $tests = array();
        foreach($files as $key=>$value)
        {
            $tests[] = new TestItem(realpath(dirname($value)), basename($value, '.src'));
        }
        return $tests;
    }

    private function scanDirectory($recursive, $dir)
    {
        $results = array();
        $cdir = scandir($dir, 1);
        foreach($cdir as $key=>$value)
        {
            if (!in_array($value,array(".","..")))
            {
                if(is_dir($dir.DIRECTORY_SEPARATOR.$value))
                {
                    if($recursive) 
                    {
                        $results = array_merge($results, self::scanDirectory($recursive, $dir.DIRECTORY_SEPARATOR.$value));
                    }
                }
                else
                {
                    $info = pathinfo($value);
                    if($info['extension'] == "src") $results[] = $dir.DIRECTORY_SEPARATOR.$value;
                }  
            }
        }
        return $results;
    }
}
$time_pre = microtime(true);
$config = parseArgs();

if($config->help)
{
    echo 
    "
    Testovací script.

    Skript pracuje s následujícími parametry:
    
    --help\t\tOtevře nápovědu (Nemůže být kombinován s žádným jiným parametrem).
    --directory=path\tTesty bude hledat v zadaném adresáři (chybí-li tento parametr, tak skript prochází aktuální adresář).
    --recursive\t\tTesty bude hledat nejen v zadaném adresáři, ale i rekurzivně ve všech jeho podadresářích.
    --parse-script=file\tSoubor se skriptem v PHP 7.4 pro analýzu zdrojového kódu v IPPcode21 (chybí-li tento parametr, tak implicitní hodnotou je parse.php uložený v aktuálním adresáři).
    --int-script=file\tSoubor se skriptem v Python 3.8 pro interpret XML reprezentace kódu v IPPcode21 (chybí-li tento parametr, tak implicitní hodnotou je interpret.py uložený v aktuálním adresáři).
    --parse-only\tBude testován pouze skript pro analýzu zdrojového kódu v IPPcode21 (tento parametr se nesmí kombinovat s parametry --int-only a --int-script).
    --int-only\t\tBude testován pouze skript pro interpret XML reprezentace kódu v IPPcode21(tento parametr se nesmí kombinovat s parametry --parse-only a --parse-script).
    --jexamxml=file\tSoubor s JAR balíčkem s nástrojem A7Soft JExamXML. Je-li parametr vynechán uvažuje se implicitní umístění /pub/courses/ipp/jexamxml/jexamxml.jar na serveru Merlin.
    --jexamcfg=file\tSoubor s konfigurací nástroje A7Soft JExamXML. Je-li parametr vynechán uvažuje se implicitní umístění /pub/courses/ipp/jexamxml/options na serveru Merlin.
    ";
    exit(ErrorCodes::Success);
}

$testItemsToBeTested = TestsLoader::getTests($config);
$testedItems = Tester::runTests($testItemsToBeTested, $config);
$time_post = microtime(true);
$exec_time = $time_post - $time_pre;
echo PageGenerator::GeneratePage($testedItems, $exec_time);



