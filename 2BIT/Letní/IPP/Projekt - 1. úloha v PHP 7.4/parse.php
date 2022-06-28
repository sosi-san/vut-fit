<?php

$ALL_OK = 0;

$PARAMETER_ERROR = 10;
$MISSING_HEADER_ERROR = 21;
$UNKONW_OP_CODE_ERRROR = 22;
$OTHER_LEX_SYN_ERROR = 23;
$args = array();
ini_set('display_errors', 'stderr');
if($argc > 1)
{
    if($argv[1] == "--help" && $argc == 2)
    {
        echo "Skript typu filtr načte ze standardního vstupu zdrojový kód v IPPcode21 zkontroluje lexikální a syntaktickou správnost kódu a vypíše na standardní výstup XML reprezentaci programu.

        Skript pracuje s následujícími parametry:
        
        --help\tOtevře nápovědu (Nemůže být kombinován s žádným jiným parametrem).
        ";
        echo "\n";
        exit($ALL_OK);
    }
    else
    {
        exit($PARAMETER_ERROR);
    }
}
$contains_header = false;
$instructionOrderNumber = 1;
while($line = fgets(STDIN))
{
    $line = SkipComments($line);
    $line = trim($line, " \n\r\t\v\0");
    if($line == "")
        continue;
    //První řádek musí být .IPPcode21
    if(!$contains_header)
    {
        if($line == ".IPPcode21")
        {
            $contains_header = true;
            echo('<?xml version="1.0" encoding="UTF-8"?>'."\n");
            echo('<program language="IPPcode21">'."\n");
            continue;
        }
        else
            exit($MISSING_HEADER_ERROR);
    }
    $splitted = explode(' ', $line);
    $splitted = preg_split('/\s+/', $line);
    switch(strtoupper($splitted[0]))
    {
        case 'ADD':
        case 'SUB':
        case 'MUL':
        case 'IDIV':
        case 'LT':
        case 'GT':
        case 'EQ':
        case 'AND':
        case 'OR':
        case 'STRI2INT':
        case 'CONCAT':
        case 'GETCHAR':
        case 'SETCHAR':
        if(count($splitted) == 4)
        {
            CheckVar($splitted[1]);
            CheckSymb($splitted[2]);
            CheckSymb($splitted[3]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);

        break;
        case 'JUMPIFEQ':
        case 'JUMPIFNEQ':
        if(count($splitted) == 4)
        {
            CheckLabel($splitted[1]);
            CheckSymb($splitted[2]);
            CheckSymb($splitted[3]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);

        break;

        case 'MOVE': 
        case 'INT2CHAR':           
        case 'STRLEN':             
        case 'TYPE':
        case 'NOT':
        if(count($splitted) == 3)
        {
            CheckVar($splitted[1]);
            CheckSymb($splitted[2]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;

        case 'READ':
        if(count($splitted) == 3)
        {
            CheckVar($splitted[1]);
            CheckType($splitted[2]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;

        case 'DEFVAR':
        case 'POPS':
        if(count($splitted) == 2)
        {
            CheckVar($splitted[1]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;

        case 'PUSHS':
        case 'WRITE':
        case 'EXIT':
        case 'DPRINT':
        if(count($splitted) == 2)
        {
            CheckSymb($splitted[1]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;

        case 'LABEL':
        case 'JUMP':
        case 'CALL':
        if(count($splitted) == 2)
        {
            CheckLabel($splitted[1]);
        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;

        case 'CREATEFRAME':
        case 'PUSHFRAME':
        case 'POPFRAME':
        case 'RETURN':
        case 'BREAK':
        if(count($splitted) == 1)
        {

        }
        else
            exit($OTHER_LEX_SYN_ERROR);
        break;
    
        default:
            exit($UNKONW_OP_CODE_ERRROR);
    }
    PrintInstruction($splitted[0], $args);
    $args = array();
}
echo('</program>'."\n");
function PrintInstruction($instruction, $args)
{
    static $instructionOrderNumber = 1;
    $argCount = 1;
    
    echo(' <instruction order="'.$instructionOrderNumber++.'" opcode="'.strtoupper($instruction).'">'."\n");
    foreach ($args as $arg) {
        echo('  <arg'.$argCount.' type="'.$arg[0]++.'">'.$arg[1]);
        echo('</arg'.$argCount.'>'."\n");
        $argCount++;
    }
    echo(' </instruction>'."\n");
}
function SkipComments($line)
{
    $matches = array();
    preg_match('/\s*#.*$/', $line, $matches, PREG_OFFSET_CAPTURE);
    if(!empty($matches))
        $line = substr($line, 0, $matches[0][1]);
    return $line;
}
function CheckVar($varToCheck)
{
    if(preg_match("/^(GF|LF|TF)@(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z])(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z]|[0-9])*$/",$varToCheck))
    {
        $varToCheck = str_replace("&", "&amp;", $varToCheck);
        $varToCheck = str_replace("<", "&lt;", $varToCheck);
        $varToCheck = str_replace(">", "&gt;", $varToCheck);
        array_push($GLOBALS['args'], array('var', $varToCheck));
        return true;
    }
    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
}
function CheckSymb($varToCheck)
{
    if(preg_match("/^(GF|LF|TF)@(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z])(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z]|[0-9])*$/",$varToCheck))
    {
        $varToCheck = str_replace("&", "&amp;", $varToCheck);
        $varToCheck = str_replace("<", "&lt;", $varToCheck);
        $varToCheck = str_replace(">", "&gt;", $varToCheck);
        array_push($GLOBALS['args'], array('var', $varToCheck));
        return true;
    }
    if(preg_match("/^(bool|nil|int|string)@/",$varToCheck))
    {
        $varToCheck = explode("@", $varToCheck);
        switch ($varToCheck[0]) {
            case 'bool':
                if(!preg_match("/^(false|true)$/",$varToCheck[1]))
                    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
                array_push($GLOBALS['args'], array('bool', $varToCheck[1]));
                break;
            case 'nil':
                if(!preg_match("/^(nil)$/",$varToCheck[1]))
                    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
                array_push($GLOBALS['args'], array('nil', $varToCheck[1]));
                break;
            case 'int':
                if($varToCheck[1] == "")
                    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);       
                 array_push($GLOBALS['args'], array('int', $varToCheck[1]));
                break;
            case 'string':
                if(!preg_match("/^(\\\\[0-9][0-9][0-9]|[^\\\\])*$/",$varToCheck[1]))
                    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
                $varToCheck[1] = str_replace("&", "&amp;", $varToCheck[1]);
                $varToCheck[1] = str_replace("<", "&lt;", $varToCheck[1]);
                $varToCheck[1] = str_replace(">", "&gt;", $varToCheck[1]);
                array_push($GLOBALS['args'], array('string', $varToCheck[1]));
                break;
            default:
                exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
                break;
        }
        return true;
    }
    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
}
function CheckLabel($varToCheck)
{
    if(preg_match("/^(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z])(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z]|[0-9])*$/",$varToCheck))
    {
        array_push($GLOBALS['args'], array('label', $varToCheck));
        return true;
    }
    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
}
function CheckType($varToCheck)
{
    if(preg_match("/^(int|string|bool)$/",$varToCheck))
    {
        array_push($GLOBALS['args'], array('type', $varToCheck));
        return true;
    }
    exit($GLOBALS["OTHER_LEX_SYN_ERROR"]);
}
?>