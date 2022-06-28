import argparse
import sys
from sys import stderr
import re
import xml.etree.ElementTree as ET

o_symb = {"var", "int", "bool", "string", "nil"}
o_lable = {"label"}
o_var = {"var"}
o_type = {"type"}

base_instructions = {}
base_instructions["ADD"] = [o_var, {"var", "int"}, {"var", "int"}]
base_instructions["SUB"] = base_instructions["ADD"]
base_instructions["MUL"] = base_instructions["ADD"]
base_instructions["IDIV"] = base_instructions["ADD"]

base_instructions["LT"] = [o_var, o_symb, o_symb]
base_instructions["GT"] = base_instructions["LT"] 
base_instructions["EQ"] = base_instructions["LT"]
base_instructions["AND"] = [o_var, {"bool", "var"}, {"bool", "var"}]
base_instructions["OR"] = base_instructions["AND"]

base_instructions["STRI2INT"] = [o_var, o_symb, o_symb]

base_instructions["CONCAT"] = [o_var, o_symb, o_symb]
base_instructions["GETCHAR"] = base_instructions["CONCAT"] 
base_instructions["SETCHAR"] = base_instructions["CONCAT"] 

base_instructions["JUMPIFEQ"] = [o_lable, o_symb, o_symb]
base_instructions["JUMPIFNEQ"] = base_instructions["JUMPIFEQ"]

base_instructions["MOVE"] = [o_var, o_symb]
base_instructions["NOT"] = [o_var, {"bool", "var"}]
base_instructions["INT2CHAR"] = [o_var, o_symb]
base_instructions["READ"] = [o_var, o_type]
base_instructions["STRLEN"] = [o_var, o_symb]
base_instructions["TYPE"] = [o_var, o_symb]


base_instructions["DEFVAR"] = [o_var]
base_instructions["WRITE"] = [o_symb]
base_instructions["CALL"] = [o_lable]

base_instructions["PUSHS"] = [o_symb]
base_instructions["POPS"] = [o_var]
base_instructions["LABEL"] = [o_lable]
base_instructions["JUMP"] = [o_lable]
base_instructions["EXIT"] = [o_symb]
base_instructions["DPRINT"] = [o_symb]

base_instructions["RETURN"] = []
base_instructions["CREATEFRAME"] = []
base_instructions["PUSHFRAME"] = []
base_instructions["POPFRAME"] = []
base_instructions["BREAK"] = []


class ErrorHandeler:
    all_ok = 0
    invalid_parameters = 10 
    input_file_error = 11
    output_file_error = 12
    wrong_xml_format = 31
    unexpected_xml_format = 32

    semantic_error = 52
    wrong_operand_type = 53
    variable_not_found = 54
    frame_not_found = 55
    value_missing = 56
    wrong_operand_value = 57
    string_operation_error = 58

class Stack():
    def __init__(self):
        self.stack = []
    def isEmpty(self):
        return self.stack == []
    def push(self, data):
        self.stack.append(data)
    def pop(self):
        if self.isEmpty():
            exit(er.frame_not_found)
        return self.stack.pop()
    def get_top(self):
        if self.isEmpty():
            exit(er.frame_not_found)
        return self.stack[len(self.stack)-1]

class StackData():
    def __init__(self):
        self.stack = []
    def isEmpty(self):
        return self.stack == []
    def push(self, data):
        self.stack.append(data)
    def pop(self):
        if self.isEmpty():
            exit(er.value_missing)
        return self.stack.pop()
    def get_top(self):
        if self.isEmpty():
            exit(er.value_missing)
        return self.stack[len(self.stack)-1]

class StackPositions():
    def __init__(self):
        self.stack = []
    def isEmpty(self):
        return self.stack == []
    def push(self, data):
        self.stack.append(data)
    def pop(self):
        if self.isEmpty():
            exit(er.value_missing)
        return self.stack.pop()
    def get_top(self):
        if self.isEmpty():
            exit(er.value_missing)
        return self.stack[len(self.stack)-1]

class Frame:
    def __init__(self):
        self.variables = {}
    def add_var(self, name, value, type):
        if name in self.variables.keys():
            exit(er.semantic_error)
        self.variables[name] = Variable(name, value, type)
    def get_var(self, name):
        if name not in self.variables.keys():
            exit(er.variable_not_found)
        return self.variables[name]

class Variable:
    def __init__(self, name, value, type):
        self.name = name
        self.value = value
        self.type = type
    def set_var(self, value, type):
        self.value = value
        self.type = type

class Arg:
    def __init__(self, type, value, order):
        self.type = type
        self.value = value
        self.order = order

class Instruction:
    def __init__(self, name, order):
        self.name = name
        self.order = order
        self.args = []
    def add_argument(self, arg_type, value, order):
        self.args.append(Arg(arg_type, value, order))

def check_var(arg):
    if not re.search("^(GF|LF|TF)@(_|-|\$|&|%|\*|!|\?|[a-z]|[A-Z])(_|-|\$|&|%|\*|!|\?|[a-z]|[A-Z]|[0-9])*$", arg.value):
        exit(er.unexpected_xml_format)
def check_lable(arg):

    if not re.search("^(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z])(_|-|\\$|&|%|\\*|!|\?|[a-z]|[A-Z]|[0-9])*$", arg.value):
        exit(er.unexpected_xml_format)
def check_type(arg):
    if not re.search("^(int|string|bool)$", arg.value):
        exit(er.unexpected_xml_format)
def check_symbol(arg):
        if arg.type == "bool":
            if not re.search("^(false|true)$", arg.value):
                exit(er.unexpected_xml_format)
        elif arg.type == "nil":
            if not re.search("^(nil)$", arg.value):
                exit(er.unexpected_xml_format)
        elif arg.type == "int":
            if not re.search("^(0|(-|)[1-9]\d*)$", arg.value):
                exit(er.unexpected_xml_format)
        elif arg.type == "string":
            if not re.search(r"^([^#\\\s]|(\\[0-9][0-9][0-9]))*$", str(arg.value)):
                exit(er.unexpected_xml_format)
        else:
            exit(er.unexpected_xml_format)
def check_if_order_is_int(order):
    try:
        return int(order)
    except ValueError:
        exit(er.unexpected_xml_format)
def check_if_is_int(var):
    try:
        int(var)
        return True
    except ValueError:
        return False
def check_if_is_string(var):
    try:
        str(var)
        return True
    except ValueError:
        return False
def check_if_instruction_is_valid(instruction):
    if instruction.name not in base_instructions:
        exit(er.unexpected_xml_format)
    if len(instruction.args) != len(base_instructions[instruction.name]):
        exit(er.unexpected_xml_format)
    # Kontroluje jestli je int opravdu int a string opravdu string
    for arg in instruction.args:
        if arg.type in ("int", "bool", "string", "nil"):
            check_symbol(arg)
        elif arg.type == "label":
            check_lable(arg)
        elif arg.type == "var":
            check_var(arg)
        elif arg.type == "type":
            check_type(arg)
        else:
            exit(er.unexpected_xml_format)

def get_var_frame(var_from_arg_value):
    return var_from_arg_value.split("@")[0]
def get_var_name(var_from_arg_value):
    return var_from_arg_value.split("@")[1]
def get_var_info(var_from_arg_value):
    frame_str = get_var_frame(var_from_arg_value)
    var_str = get_var_name(var_from_arg_value)
    return get_frame(frame_str), get_frame(frame_str).get_var(var_str)
def check_instruction_operands(instruction):
    for index, arg in enumerate(instruction.args):
        if arg.type not in base_instructions[instruction.name][index]:
            exit(er.wrong_operand_type)
def var_has_value(var):
    if var.value is None:
        exit(er.value_missing)
def get_frame(frame):
    if frame == "GF":
        frame = GLOBAL_FRAME
    elif frame == "LF":
        frame = LOCAL_FRAME
    else:
        frame = TEMPORARY_FRAME
    if frame == None:
        exit(er.frame_not_found)
    return frame
def string_regex(string):
    msg_tmp = string
    for match in re.finditer(r'\\[0-9][0-9][0-9]', msg_tmp):
        char_int = int(match.group(0).replace("\\0", ""))
        string = string.replace(match.group(0), chr(char_int))
    return string
def interpret(instruction):
    global instruction_index
    # Kontroluje jestli je instrukce dostává na vstup to co má
    check_instruction_operands(instruction)
    arg = instruction.args
    if len(arg) == 3:
        if instruction.name in ("ADD", "SUB", "MUL", "IDIV"):
            left_num = arg[1].value
            right_num = arg[2].value
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                left_num = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                right_num = var.value
            frame, var = get_var_info(arg[0].value)
            if instruction.name == "ADD":
                var.set_var(int(left_num)+int(right_num), "int")
            elif instruction.name == "SUB":
                var.set_var(int(left_num)-int(right_num), "int")
            elif instruction.name == "MUL":
                var.set_var(int(left_num)*int(right_num), "int")
            elif instruction.name == "IDIV":
                if int(right_num) == 0:
                    exit(er.wrong_operand_value)
                var.set_var(int(left_num)//int(right_num), "int")
        elif instruction.name == "STRI2INT":
            frame, var = get_var_info(arg[0].value)
            string = ""
            index = 0
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                string = var.value
            elif arg[1].type == "string":
                string = arg[1].value
            else:
                exit(er.wrong_operand_type)
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                index = var.value
            elif arg[2].type == "int":
                index = arg[2].value
            else:
                exit(er.wrong_operand_type)
            
            value = ""
            index = int(index)
            if index >= len(string) or index < 0:
                exit(er.string_operation_error)
            value = ord(string[index])
            var.set_var(value, "int")
        elif instruction.name == "CONCAT":
            frame, var = get_var_info(arg[0].value)
            string_a = ""
            string_b = ""
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                string_a = var.value
            elif arg[1].type == "string":
                string_a = arg[1].value
            else:
                exit(er.wrong_operand_type)
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                string_b = var.value
            elif arg[2].type == "string":
                string_b = arg[2].value
            else:
                exit(er.wrong_operand_type)
            
            string = string_a + string_b
            var.set_var(string, "string")
        elif instruction.name == "GETCHAR":
            frame, var = get_var_info(arg[0].value)
            string = ""
            index = 0
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                string = var.value
            elif arg[1].type == "string":
                string = arg[1].value
            else:
                exit(er.wrong_operand_type)
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                index = var.value
            elif arg[2].type == "int":
                index = arg[2].value
            else:
                exit(er.wrong_operand_type)
            
            value = ""
            index = int(index)
            if index >= len(string) or index < 0:
                exit(er.string_operation_error)
            value = string[index]
            var.set_var(value, "string")
        elif instruction.name == "SETCHAR":
            frame, var = get_var_info(arg[0].value)
            var_has_value(var)
            if var.type != "string":
                    exit(er.wrong_operand_type)
            string = var.value
            char = ""
            index = 0
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                index = var.value
            elif arg[1].type == "int":
                index = arg[1].value
            else:
                exit(er.wrong_operand_type)
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                char = var.value
            elif arg[2].type == "string":
                char = arg[2].value
                char = string_regex(char)
            else:
                exit(er.wrong_operand_type)
            

            if len(char) < 1:
                exit(er.string_operation_error)
            char = char[0]
            index = int(index)
            if index >= len(string) or index < 0:
                exit(er.string_operation_error)
            string = list(string)
            string[index] = char
            string = "".join(string)
            var.set_var(string, "string")
        elif instruction.name == "JUMPIFEQ":
            if arg[0].value not in lables.keys():
                exit(er.semantic_error)
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            if type_a == type_b and type_a != "nil":
                if type_a == "int":
                    if int(value_a) == int(value_b):
                        return lables[arg[0].value]
                    else:
                        return None
                elif type_a == "string" or type_a == "bool":
                    value_a = string_regex(value_a)
                    value_b = string_regex(value_b)
                    if str(value_a) == str(value_b):
                        return lables[arg[0].value]
                    else:
                        return None
            elif type_a == "nil" or type_b == "nil":
                if type_a == "nil" and type_b == "nil":
                    return lables[arg[0].value]
                else:
                    return None
            else:
                exit(er.wrong_operand_type)
        elif instruction.name == "JUMPIFNEQ":
            if arg[0].value not in lables.keys():
                exit(er.semantic_error)
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            if type_a == type_b:
                if type_a == "int":
                    if int(value_a) != int(value_b):
                        return lables[arg[0].value]
                    else:
                        return None
                elif type_a == "string" or type_a == "bool":
                    value_a = string_regex(value_a)
                    value_b = string_regex(value_b)
                    if str(value_a) != str(value_b):
                        return lables[arg[0].value]
                    else:
                        return None
            elif type_a == "nil" or type_b == "nil":
                if type_a == "nil" and type_b != "nil" or type_a != "nil" and type_b == "nil":
                    return lables[arg[0].value]
                else:
                    return None
            else:
                exit(er.wrong_operand_type)
        elif instruction.name == "EQ":
            frame, var = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            match = "false"
            if type_a == "nil" or type_b == "nil":
                if (type_a == type_b):
                    match = "true"
            elif type_a != type_b:
                exit(er.wrong_operand_type)
            else:
                if type_a == "string":
                    value_a = string_regex(value_a)
                    value_b = string_regex(value_b)
                    if len(value_a) == len(value_b):
                        match = "true"
                else:
                    if str(value_a) == str(value_b):
                        match = "true"
            var.set_var(str(match), "bool")
        elif instruction.name == "GT":
            frame, var = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            match = False
            if type_a == "nil" or type_b == "nil":
                exit(er.wrong_operand_type)
            if type_a != type_b:
                exit(er.wrong_operand_type)
            if type_a == "string":
                value_a = string_regex(value_a)
                value_b = string_regex(value_b)
                match = value_a > value_b
            elif type_a == "int":
                match = int(value_a) > int(value_b)
            else:
                if value_a == "true" and value_b == "false":
                    match = True
            if match:
                match = "true"
            else:
                match = "false"
            var.set_var(str(match), "bool")
        elif instruction.name == "LT":
            frame, var = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            match = False
            if type_a == "nil" or type_b == "nil":
                exit(er.wrong_operand_type)
            if type_a != type_b:
                exit(er.wrong_operand_type)
            if type_a == "string":
                value_a = string_regex(value_a)
                value_b = string_regex(value_b)
                match = value_a < value_b
            elif type_a == "int":
                match = int(value_a) < int(value_b)
            else:
                if value_a == "false" and value_b == "true":
                    match = True
                else:
                    match = False
            if match:
                match = "true"
            else:
                match = "false"
            var.set_var(match, "bool")
        elif instruction.name == "AND":
            frame, var_to = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = var.value
            if type_a != type_b and type_a != "bool":
                exit(er.wrong_operand_type)
            match = ""
            if value_a == "true" and value_b == "true":
                match = "true"
            else:
                match = "false"
            var_to.set_var(match, "bool")
        elif instruction.name == "OR":
            frame, var_this = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            type_b = arg[2].type
            value_a = arg[1].value
            value_b = arg[2].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = str(var.value)
            if arg[2].type == "var":
                frame, var = get_var_info(arg[2].value)
                var_has_value(var)
                type_b = var.type
                value_b = str(var.value)
            if type_a != type_b and type_a != "bool":
                exit(er.wrong_operand_type)
            match = "false"
            if value_a == "true":
                match = "true"
            elif value_b == "true":
                match = "true"
            var_this.set_var(match, "bool")
    if len(instruction.args) == 2:
        if instruction.name == "MOVE":
            frame, var = get_var_info(arg[0].value)
            value = ""
            type = ""
            if arg[1].type == "var":
                frame_from, var_from = get_var_info(arg[1].value)
                var_has_value(var)
                value = var_from.value
                type = var_from.type
            else:
                value = arg[1].value
                type = arg[1].type

            var.set_var(value, type)
        elif instruction.name == "READ":
            v_value = "nil"
            v_type = "nil"
            if input_file == None:
                v_value = input()
            else:
                v_value = input_file.readline()
            if v_value != "":
                v_value = v_value[:-1]
                if arg[1].value == "int":
                    if check_if_is_int(v_value):
                        v_value = int(v_value)
                        v_type = "int"
                    else:
                        v_value = "nil"
                elif arg[1].value == "string":
                    if check_if_is_string(v_value):
                        v_value = string_regex(v_value)
                        v_type = "string"
                    else:
                        v_value = "nil"
                else:
                    v_type = "bool"
                    v_value = v_value.rstrip()
                    if v_value.lower() == "true":
                        v_value = "true"
                    else:
                        v_value = "false"
            frame, var = get_var_info(arg[0].value)
            var.set_var(v_value, v_type)
        elif instruction.name == "TYPE":
            v_type = ""
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                if var.value is not None:
                    v_type = var.type
            else:
                v_type = arg[1].type
            frame, var = get_var_info(arg[0].value)
            var.set_var(str(v_type), "string")
        elif instruction.name == "INT2CHAR":
            frame, var_to = get_var_info(arg[0].value)
            value = ""
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                value = var.value
            elif arg[1].type == "int":
                value = arg[1].value
            else:
                exit(er.wrong_operand_type)

            try:
                value = chr(int(value))
            except:
                exit(er.string_operation_error)

            var_to.set_var(value, "string")
        elif instruction.name == "STRLEN":
            frame, var = get_var_info(arg[0].value)
            string = ""
            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                if var.type != "string":
                    exit(er.wrong_operand_type)
                string = var.value
            elif arg[1].type == "string":
                string = arg[1].value
            else:
                exit(er.wrong_operand_type)
            var.set_var(len(str(string)), "int")
        elif instruction.name == "NOT":
            frame, var = get_var_info(arg[0].value)
            index = 0
            type_a = arg[1].type
            value_a = arg[1].value

            if arg[1].type == "var":
                frame, var = get_var_info(arg[1].value)
                var_has_value(var)
                type_a = var.type
                value_a = var.value
            if type_a != "bool":
                exit(er.wrong_operand_type)
            match = "true"
            if value_a == "true":
                match = "false"
            var.set_var(str(match), "bool")


    elif len(instruction.args) == 1:
        if instruction.name == "DEFVAR":
            frame = get_frame(get_var_frame(arg[0].value))
            frame.add_var(get_var_name(arg[0].value), None, None)
        elif instruction.name == "WRITE":
            msg = arg[0].value
            is_string = False
            if arg[0].type == "var":
                frame, var = get_var_info(arg[0].value)
                var_has_value(var)
                if var.type == "string":
                    is_string = True
                if var.type == "nil":
                    msg = ""
                else:
                    msg = var.value
            elif arg[0].type == "nil":
                msg = ""
            elif arg[0].type == "bool":
                msg = str(arg[0].value)
            elif arg[0].type == "string":
                is_string = True
            if is_string:
                msg = string_regex(msg)
            print(msg, end='')
        elif instruction.name == "EXIT":
            value = -1 #bad value
            if arg[0].type == "var":
                frame, var = get_var_info(arg[0].value)
                var_has_value(var)
                if var.type != "int":
                    exit(er.wrong_operand_type)
                value = int(var.value)
            elif arg[0].type == "int":
                value = int(arg[0].value)
            else:
                exit(er.wrong_operand_type)
            if value < 0 or value > 49:
                    exit(er.wrong_operand_value)
            exit(value)
        elif instruction.name == "LABEL":
            return None   
        elif instruction.name == "JUMP":
            if arg[0].value not in lables.keys():
                exit(er.semantic_error)
            return lables[arg[0].value]
        elif instruction.name == "PUSHS":
            value = arg[0].value
            if arg[0].type == "var":
                frame, var = get_var_info(arg[0].value)
                var_has_value(var)
            DATA_STACK.push(value)
        elif instruction.name == "POPS":
            frame, var = get_var_info(arg[0].value)
            var.set_var(DATA_STACK.pop(), "int")
        elif instruction.name == "CALL":
            if arg[0].value not in lables.keys():
                exit(er.semantic_error)
            POSITION_STACK.push(instruction_index)
            return lables[arg[0].value]
        elif instruction.name == "DPRINT":
            return None

    else:
        global LOCAL_FRAME
        global TEMPORARY_FRAME
        if instruction.name == "CREATEFRAME":
            TEMPORARY_FRAME = Frame()
        elif instruction.name == "PUSHFRAME":
            if TEMPORARY_FRAME is None:
                exit(er.frame_not_found)   
            FRAME_STACK.push(TEMPORARY_FRAME)
            TEMPORARY_FRAME = None
            LOCAL_FRAME = FRAME_STACK.get_top()
        elif instruction.name == "POPFRAME":  
            TEMPORARY_FRAME = FRAME_STACK.pop()
            LOCAL_FRAME = FRAME_STACK.get_top()
        elif instruction.name == "RETURN":
            instruction_index = POSITION_STACK.pop()
        elif instruction.name == "BREAK":
            return None
    return None
# argparse

ap = argparse.ArgumentParser()
er = ErrorHandeler
ap.add_argument("--source", nargs=1, help="Vstupní soubor IPPcode21 ve XML formátu")
ap.add_argument("--input", nargs=1, help="Vstupní hodnoty pro instrukci read")


arguments = vars(ap.parse_args())

source_file = arguments["source"]
input_file = arguments["input"]
if source_file == None and input_file == None:
    exit(er.invalid_parameters)
if source_file == None:
    source_file = [sys.stdin]
if input_file != None:
    try:
        input_file = open(input_file[0], "r")
    except:
        exit(er.input_file_error)

try:
    tree = ET.parse(source_file[0])
except FileNotFoundError:
    exit(er.input_file_error)
except Exception:
    exit(er.wrong_xml_format)
except:
    exit(er.input_file_error)

# Kontrola správnosti XML stromu
root = tree.getroot()

if root.tag != "program":
    exit(er.unexpected_xml_format)
for child in root:
    if child.tag != "instruction":
        exit(er.unexpected_xml_format)
    ca = list(child.attrib.keys())
    if not("order" in ca) or not("opcode" in ca):
        exit(er.unexpected_xml_format)
    for element in child:
        if not(re.match(r"arg[123]", element.tag)):
            exit(er.unexpected_xml_format)

instructions = []
# Prevod XML na instrukce
for instruction in root:
    instructions.append(Instruction(instruction.attrib.get("opcode").upper(), check_if_order_is_int(instruction.attrib.get("order"))))
    for argument in instruction:
        order = argument.tag.split("arg")[1]
        if argument.text == None:
            argument.text = ""
        instructions[-1].add_argument(str(argument.attrib.get("type")), str(argument.text), int(order))
    instructions[-1].args.sort(key=lambda x: x.order)
    for argument_index, argument in enumerate(instructions[-1].args):
        order = argument_index + 1
        if order != int(argument.order):
            exit(er.unexpected_xml_format)
if len(instructions) > 0:
    # Seřazení instrukcí podle orderu
    instructions.sort(key=lambda x: x.order)
    # Kontrola duplicity a negativity orderu
    for index, instruction in enumerate(instructions):
        if instruction.order < 1:
            exit(er.unexpected_xml_format)
        for index_b, other_inst in enumerate(instructions):
            if index == index_b:
                continue
            if instruction.order == other_inst.order:
                exit(er.unexpected_xml_format)  
    lables = {}
    # První průchod pro získání labelů a Ma instrukce spravny pocet operandu a exituje vůbec
    for index, instruction in enumerate(instructions):
        check_if_instruction_is_valid(instruction)
        instruction.order = index
        if instruction.name == "LABEL":
            lableName = instruction.args[0].value
            if lableName not in lables.keys():
                lables[lableName] = instruction
            else:
                exit(er.semantic_error) 

    # Zásobník rámců a GF
    FRAME_STACK = Stack()
    DATA_STACK = StackData()
    POSITION_STACK = StackPositions()
    GLOBAL_FRAME = Frame()
    LOCAL_FRAME = None
    TEMPORARY_FRAME = None
    instruction_index = 0
    while instruction_index < len(instructions):

        current_instruction = instructions[instruction_index]
        # Proved instrukci
        label = interpret(current_instruction)
        if label is not None:
            instruction_index = label.order
        else:
            instruction_index += 1
if input_file != None:
    input_file.close()
exit(er.all_ok) 