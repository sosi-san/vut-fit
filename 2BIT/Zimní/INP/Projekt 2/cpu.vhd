-- cpu.vhd: Simple 8-bit CPU (BrainF*ck interpreter)
-- Copyright (C) 2019 Brno University of Technology,
--                    Faculty of Information Technology
-- Author(s): DOPLNIT
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

-- ----------------------------------------------------------------------------
--                        Entity declaration
-- ----------------------------------------------------------------------------
entity cpu is
 port (
   CLK   : in std_logic;  -- hodinovy signal
   RESET : in std_logic;  -- asynchronni reset procesoru
   EN    : in std_logic;  -- povoleni cinnosti procesoru
 
   -- synchronni pamet RAM
   DATA_ADDR  : out std_logic_vector(12 downto 0); -- adresa do pameti
   DATA_WDATA : out std_logic_vector(7 downto 0); -- mem[DATA_ADDR] <- DATA_WDATA pokud DATA_EN='1'
   DATA_RDATA : in std_logic_vector(7 downto 0);  -- DATA_RDATA <- ram[DATA_ADDR] pokud DATA_EN='1'
   DATA_RDWR  : out std_logic;                    -- cteni (0) / zapis (1)
   DATA_EN    : out std_logic;                    -- povoleni cinnosti
   
   -- vstupni port
   IN_DATA   : in std_logic_vector(7 downto 0);   -- IN_DATA <- stav klavesnice pokud IN_VLD='1' a IN_REQ='1'
   IN_VLD    : in std_logic;                      -- data platna
   IN_REQ    : out std_logic;                     -- pozadavek na vstup data
   
   -- vystupni port
   OUT_DATA : out  std_logic_vector(7 downto 0);  -- zapisovana data
   OUT_BUSY : in std_logic;                       -- LCD je zaneprazdnen (1), nelze zapisovat
   OUT_WE   : out std_logic                       -- LCD <- OUT_DATA pokud OUT_WE='1' a OUT_BUSY='0'
 );
end cpu;


-- ----------------------------------------------------------------------------
--                      Architecture declaration
-- ----------------------------------------------------------------------------
architecture behavioral of cpu is

 -- zde dopiste potrebne deklarace signalu
	signal pc_reg : std_logic_vector(12 downto 0);
	signal pc_inc : std_logic;
	signal pc_dec : std_logic;
	
	signal pointer_reg : std_logic_vector(12 downto 0);
	signal pointer_inc : std_logic;
	signal pointer_dec : std_logic;
	
	
	signal multiplex_1 : std_logic;
	signal multiplex_2 : std_logic;
	signal multiplex_2_value : std_logic_vector(12 downto 0);
	signal tmp_reg_addr : std_logic_vector(12 downto 0) := "1000000000000";
	signal multiplex_3 : std_logic_vector(1 downto 0) := "00";
	
	type fsm_state is (
		s_idle,
		s_ins_get,
		s_ins_decode,
		s_inc_ptr,
		s_dec_ptr,
		s_inc_a,
		s_dec_a,
		s_inc_b,
		s_dec_b,
		s_printchar_a,
		s_printchar_b,
		s_getchar_a,
		s_getchar_b,
		s_while_start_a,
		s_while_end_a,
		s_while_start_b,
		s_while_end_b,
		s_while_start_c,
		s_while_end_c,
		s_while_start_d,
		s_while_end_d,
		s_save_tmp,
		s_get_tmp_a,
		s_get_tmp_b,
		s_set_tmp_a,
		s_set_tmp_b,
		s_default,
		s_halt
		);

	signal fsm_curr_state : fsm_state := s_idle;
	signal fsm_next_state : fsm_state;
		
	
	
	


begin

 -- zde dopiste vlastni VHDL kod


 -- pri tvorbe kodu reflektujte rady ze cviceni INP, zejmena mejte na pameti, ze 
 --   - nelze z vice procesu ovladat stejny signal,
 --   - je vhodne mit jeden proces pro popis jedne hardwarove komponenty, protoze pak
 --   - u synchronnich komponent obsahuje sensitivity list pouze CLK a RESET a 
 --   - u kombinacnich komponent obsahuje sensitivity list vsechny ctene signaly.
 
	pc_counter: process (CLK, RESET)
	begin
	
		if RESET =  '1' then
			pc_reg <= (others => '0');
			
		elsif rising_edge(CLK) then
					
			if pc_dec = '1' then
				pc_reg <= pc_reg - 1;
				
			elsif pc_inc = '1' then
				pc_reg <= pc_reg + 1;
				
			end if;
			
		end if;
		
	end process;
	
	ptr_counter: process (CLK, RESET)
	begin
	
		if RESET =  '1' then
			pointer_reg <=  tmp_reg_addr;
			
		elsif rising_edge(CLK) then
				
			if pointer_dec = '1' then
				pointer_reg <= pointer_reg - 1;
				
			elsif pointer_inc = '1' then
				pointer_reg <= pointer_reg + 1;
				
			end if;
			
		end if;
		
	end process;

	multiplex_1_proc: process (CLK, multiplex_1, pc_reg, multiplex_2_value)
	begin
		case multiplex_1 is
			when '0' =>
				DATA_ADDR <= pc_reg;
			when '1' =>
				DATA_ADDR <= multiplex_2_value;
			when others =>
				null;
		end case;
			
	end process;
	
	multiplex_2_proc: process (CLK, multiplex_2, pointer_reg)
	begin
		case multiplex_2 is
			when '0' =>
				multiplex_2_value <= pointer_reg;
			when '1' =>
				multiplex_2_value <= tmp_reg_addr;
			when others =>
				null;
		end case;
			
	end process;
	
	
	multiplex_3_proc: process (CLK, multiplex_3, IN_DATA, DATA_RDATA)
	begin
			case multiplex_3 is
				when "00" =>
					DATA_WDATA <= IN_DATA;
				when "01" =>
					DATA_WDATA <= DATA_RDATA - 1;
				when "10" =>
					DATA_WDATA <= DATA_RDATA + 1;
				when others =>
					DATA_WDATA <= DATA_RDATA;
			end case;
	end process;
	
	
	fsm_curr_state_proc: process (CLK, RESET)
	begin
		if RESET = '1' then
			fsm_curr_state <= s_idle;
		elsif rising_edge(CLK) then
			if EN = '1' then
				fsm_curr_state <= fsm_next_state;
			end if;
		end if;
	end process;
	
	fsm_next_state_proc: process (fsm_curr_state, OUT_BUSY, IN_VLD, DATA_RDATA)
	begin
			

		pointer_inc <= '0';
		pc_inc <= '0';
		
		pointer_dec <= '0';
		pc_dec <= '0';

		multiplex_1 <= '0';
		multiplex_2 <= '0';
		multiplex_3 <= "11";
		
		OUT_WE <= '0';
		IN_REQ <= '0';
		DATA_EN <= '0';
		DATA_RDWR <= '0';	
		
		case fsm_curr_state is
		
			---------------
			-- VYCHOZY STAV
			---------------
			when s_idle =>		
				fsm_next_state <= s_ins_get;
			
			---------------
			-- NACTENI DALSI INSTRUKCE
			---------------
			when s_ins_get =>
				DATA_EN <= '1';				
				fsm_next_state <= s_ins_decode;
				multiplex_1 <= '0';
				
			---------------
			-- DECODER PRIKAZU
			---------------
			when s_ins_decode =>
			
				case DATA_RDATA is
					
					when X"3E" =>
						fsm_next_state <= s_inc_ptr;
						
					when X"3C" =>
						fsm_next_state <= s_dec_ptr;
						
					when X"2B" =>
						fsm_next_state <= s_inc_a;
						
					when X"2D" =>
						fsm_next_state <= s_dec_a;
						
					when X"2E" =>
						fsm_next_state <= s_printchar_a;
						
					when X"2C" =>
						fsm_next_state <= s_getchar_a;
						
					when X"5B" =>
						fsm_next_state <= s_while_start_a;
						
					when X"5D" =>
						fsm_next_state <= s_while_end_a;
						
					when X"21" =>
						fsm_next_state <= s_get_tmp_a;
					
					when X"24" =>
						fsm_next_state <= s_set_tmp_a;
											
					when X"00" =>
						fsm_next_state <= s_halt;
						
					when others =>
						fsm_next_state <= s_default;
						
						
				end case;
				
			---------------
			-- INC POINER
			---------------	
			when s_inc_ptr =>
				pointer_inc <= '1';
				pc_inc <= '1';
				
				fsm_next_state <= s_ins_get;
				
			---------------
			-- DEC POINER
			---------------	
			when s_dec_ptr =>
				pointer_dec <= '1';
				pc_inc <= '1';
				
				fsm_next_state <= s_ins_get;
				
			---------------
			-- INC DATA
			---------------	
			when s_inc_a =>
				DATA_EN <= '1';
				multiplex_1 <= '1';
				
				fsm_next_state <= s_inc_b;
				
			when s_inc_b =>
				DATA_EN <= '1';
				DATA_RDWR <= '1';
				multiplex_1 <= '1';
				multiplex_3 <= "10";
				pc_inc <= '1';
				fsm_next_state <= s_ins_get;
				
			---------------
			-- DEC DATA
			---------------	
			when s_dec_a =>
			
				DATA_EN <= '1';
				multiplex_1 <= '1';
				
				fsm_next_state <= s_dec_b;
				
			when s_dec_b =>
			
				DATA_EN <= '1';
				DATA_RDWR <= '1';
				multiplex_1 <= '1';
				multiplex_3 <= "01";
				pc_inc <= '1';
				fsm_next_state <= s_ins_get;
				
			---------------
			-- OUTPUT
			---------------
			
			when s_printchar_a =>
				DATA_EN <= '1';
				multiplex_1 <= '1';
				multiplex_2 <= '0';
				fsm_next_state <= s_printchar_b;
				
			when s_printchar_b =>
				fsm_next_state <= s_printchar_b;
				if OUT_BUSY = '0' then
					OUT_WE <= '1';
					OUT_DATA <= DATA_RDATA;
					pc_inc <= '1';
					fsm_next_state <= s_ins_get;
				end if;
				
			---------------
			-- INPUT
			---------------
			
			when s_getchar_a =>
				IN_REQ <= '1';
				fsm_next_state <= s_getchar_b;
				
			when s_getchar_b =>
				IN_REQ <= '1';
				fsm_next_state <= s_getchar_b;
				if IN_VLD = '1' then
					DATA_EN <= '1';
					DATA_RDWR <= '1';
					multiplex_1 <= '1';
					multiplex_3 <= "00";
					pc_inc <= '1';
					fsm_next_state <= s_ins_get;
				end if;
				
			---------------
			-- WHILE START
			---------------
				
			when s_while_start_a =>
				pc_inc <= '1';
				DATA_EN <= '1';
				multiplex_1 <= '1';
				--pc_inc <= '1';
				
				fsm_next_state <= s_while_start_b;
			
			when s_while_start_b =>
				fsm_next_state <= s_while_start_c;
				
				if DATA_RDATA /= "00000000" then
					fsm_next_state <= s_ins_get;
				end if;

			when s_while_start_c =>
			
				DATA_EN <= '1';
				multiplex_1 <= '0';
				fsm_next_state <= s_while_start_d;
				
			when s_while_start_d =>
			
				pc_inc <= '1';
				fsm_next_state <= s_while_start_c;				
				if DATA_RDATA = X"5D" then
					fsm_next_state <= s_ins_get;
				end if;
				
			---------------
			-- WHILE END
			---------------
			when s_while_end_a =>
				pc_inc <= '1';
				DATA_EN <= '1';
				multiplex_1 <= '1';
				--pc_inc <= '1';
				
				fsm_next_state <= s_while_end_b;
			
			when s_while_end_b =>
				fsm_next_state <= s_ins_get;
				
				if DATA_RDATA /= "00000000" then
					fsm_next_state <= s_while_end_c;
				end if;

			when s_while_end_c =>
			
				DATA_EN <= '1';
				multiplex_1 <= '0';
				fsm_next_state <= s_while_end_d;
				
			when s_while_end_d =>
			
				fsm_next_state <= s_while_end_c;				
				if DATA_RDATA = X"5B" then
					fsm_next_state <= s_ins_get;
				end if;
				pc_inc <= '1';
				
			---------------
			-- TMP GET
			---------------
			when s_get_tmp_a =>
				multiplex_1 <= '1';
				multiplex_2 <= '1';
				DATA_EN <= '1';
				pc_inc <= '1';
				fsm_next_state <= s_get_tmp_b;
			
			when s_get_tmp_b =>
					DATA_EN <= '1';
					DATA_RDWR <= '1';
					multiplex_1 <= '1';
					multiplex_2 <= '0';
					multiplex_3 <= "11";
					fsm_next_state <= s_ins_get;
					
			---------------
			-- TMP SET
			---------------
			when s_set_tmp_a =>
				multiplex_1 <= '1';
				multiplex_2 <= '0';
				DATA_EN <= '1';
				pc_inc <= '1';
				fsm_next_state <= s_set_tmp_b;
			
			when s_set_tmp_b =>
					DATA_EN <= '1';
					DATA_RDWR <= '1';
					multiplex_1 <= '1';
					multiplex_2 <= '1';
					multiplex_3 <= "11";
					fsm_next_state <= s_ins_get;
					
			---------------
			-- HALT
			---------------
			when s_halt =>
				fsm_next_state <=s_halt;
				
			when s_default =>
				fsm_next_state <= s_ins_get;
				pc_inc <= '1';			
			when others =>
				null;
				
		end case;
		
	end process;
				
	
 
end behavioral;
 

