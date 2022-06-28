-- Autor reseni: Adam Woska xwoska00

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

entity ledc8x8 is
port (
	LED : out std_logic_vector(0 to 7); 
	ROW : out std_logic_vector(0 to 7);
	SMCLK : in std_logic;
	RESET : in std_logic
);
end ledc8x8;

architecture main of ledc8x8 is

    signal count : std_logic_vector(11 downto 0) := (others => '0');
    signal delay_count : std_logic_vector(22 downto 0) := (others => '0');
    signal cn : std_logic;
    signal led_i : std_logic_vector(7 downto 0) := (others => '0');
    signal row_i : std_logic_vector(7 downto 0) := (others => '0');
    signal states : std_logic_vector(1 downto 0) := (others => '0');

    begin
        normal_counter: process(SMCLK, RESET)
        begin
            if RESET = '1' then
                count <= (others => '0');
            elsif rising_edge(SMCLK) then
                count <= count + 1;
                if count = "111000010000" then
                    cn <= '1';
                else
                    cn <= '0';
                end if;
            end if;
        end process normal_counter;
        
        row_rotation: process(SMCLK, RESET, cn)
        begin
            if RESET = '1' then
                row_i <= "10000000";
            elsif SMCLK'event and SMCLK = '1' then
                if cn = '1' and (states = "00" or states = "11") then
                    row_i <= row_i(0) & row_i(7 downto 1);
                end if;
            end if;
        end process row_rotation;

        --- Zmena stavu podle toho jestli ubehla 0.5s a take pocitadlo zpozdeni
        state_counter: process(SMCLK, RESET)
        begin
            if RESET = '1' then
                delay_count <= (others => '0');
            elsif SMCLK'event and SMCLK = '1' then
                delay_count <= delay_count + 1;
                if delay_count = "1110000100000000000000" and states = "00" then
                    states <=  "01";
                    delay_count <= (others => '0');
                elsif delay_count = "1110000100000000000000" and states = "01" then
                    states <=  "11";
                    delay_count <= (others => '0');
                end if;
            end if;
        end process state_counter;

        display: process(row_i, states)
        begin
            if states = "01" then
                case row_i is
                    when "10000000" => led_i <= "11111111";
                    when "01000000" => led_i <= "11111111";
                    when "00100000" => led_i <= "11111111";
                    when "00010000" => led_i <= "11111111";
                    when "00001000" => led_i <= "11111111";
                    when "00000100" => led_i <= "11111111";
                    when "00000010" => led_i <= "11111111";
                    when "00000001" => led_i <= "11111111";
                    when others => led_i <= (others => '1');
                end case;
            else
                case row_i is
                    when "10000000" => led_i <= "11101111";
                    when "01000000" => led_i <= "11010111";
                    when "00100000" => led_i <= "11000111";
                    when "00010000" => led_i <= "11010111";
                    when "00001000" => led_i <= "10111011";
                    when "00000100" => led_i <= "10111011";
                    when "00000010" => led_i <= "10101011";
                    when "00000001" => led_i <= "11010111";
                    when others => led_i <= (others => '1');
                end case;
            end if;
        end process display;

        LED <= led_i;
        ROW <= row_i;

end main;