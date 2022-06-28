-- fsm.vhd: Finite State Machine
-- Author(s): 
--
library ieee;
use ieee.std_logic_1164.all;
-- ----------------------------------------------------------------------------
--                        Entity declaration
-- ----------------------------------------------------------------------------
entity fsm is
port(
   CLK         : in  std_logic;
   RESET       : in  std_logic;

   -- Input signals
   KEY         : in  std_logic_vector(15 downto 0);
   CNT_OF      : in  std_logic;

   -- Output signals
   FSM_CNT_CE  : out std_logic;
   FSM_MX_MEM  : out std_logic;
   FSM_MX_LCD  : out std_logic;
   FSM_LCD_WR  : out std_logic;
   FSM_LCD_CLR : out std_logic
);
end entity fsm;

-- ----------------------------------------------------------------------------
--                      Architecture declaration
-- ----------------------------------------------------------------------------
-- woska00 : kod1 = 1254493985 	 kod2 = 1254886768

architecture behavioral of fsm is
   type t_state is (FINAL, FIRST_NUMBER, PRINT_MESSAGE_ERROR, PRINT_MESSAGE_SUCCESS, FINISH, BAD_NUMBER, SECOND_NUMBER, THIRD_NUMBER, FOURTH_NUMBER, FIFTH_NUMBER, SIXTH_NUMBER_1, SIXTH_NUMBER_2, SEVENTH_NUMBER_1, SEVENTH_NUMBER_2, EIGHTH_NUMBER_1, EIGHTH_NUMBER_2, NINETH_NUMBER_1, NINETH_NUMBER_2, TENTH_NUMBER_1, TENTH_NUMBER_2);
   signal present_state, next_state : t_state;

begin
-- -------------------------------------------------------
sync_logic : process(RESET, CLK)
begin
   if (RESET = '1') then
      present_state <= FIRST_NUMBER;
   elsif (CLK'event AND CLK = '1') then
      present_state <= next_state;
   end if;
end process sync_logic;

-- -------------------------------------------------------
next_state_logic : process(present_state, KEY, CNT_OF)
begin
   case (present_state) is
   -- - - - - - - - - - - - - - - - - - - - - - -
   when FIRST_NUMBER =>
      next_state <= FIRST_NUMBER;
		if (KEY(1) = '1') then
         next_state <= SECOND_NUMBER; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when SECOND_NUMBER =>
      next_state <= SECOND_NUMBER;
		if (KEY(2) = '1') then
         next_state <= THIRD_NUMBER; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when THIRD_NUMBER =>
      next_state <= THIRD_NUMBER;
		if (KEY(5) = '1') then
         next_state <= FOURTH_NUMBER; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;

   -- - - - - - - - - - - - - - - - - - - - - - -
	 when FOURTH_NUMBER =>
      next_state <= FOURTH_NUMBER;
		if (KEY(4) = '1') then
         next_state <= FIFTH_NUMBER; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when FIFTH_NUMBER =>
      next_state <= FIFTH_NUMBER;
		if (KEY(4) = '1') then
         next_state <= SIXTH_NUMBER_1;
		elsif (KEY(8) = '1') then
         next_state <= SIXTH_NUMBER_2;
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when SIXTH_NUMBER_1 =>
      next_state <= SIXTH_NUMBER_1;
		if (KEY(9) = '1') then
         next_state <= SEVENTH_NUMBER_1; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when SEVENTH_NUMBER_1 =>
      next_state <= SEVENTH_NUMBER_1;
		if (KEY(3) = '1') then
         next_state <= EIGHTH_NUMBER_1; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when EIGHTH_NUMBER_1 =>
      next_state <= EIGHTH_NUMBER_1;
		if (KEY(9) = '1') then
         next_state <= NINETH_NUMBER_1; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when NINETH_NUMBER_1 =>
      next_state <= NINETH_NUMBER_1;
		if (KEY(8) = '1') then
         next_state <=  TENTH_NUMBER_1; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when TENTH_NUMBER_1 =>
      next_state <= TENTH_NUMBER_1;
		if (KEY(5) = '1') then
         next_state <=  FINAL; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when SIXTH_NUMBER_2 =>
      next_state <= SIXTH_NUMBER_2;
		if (KEY(8) = '1') then
         next_state <=  SEVENTH_NUMBER_2; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when SEVENTH_NUMBER_2 =>
      next_state <= SEVENTH_NUMBER_2;
		if (KEY(6) = '1') then
         next_state <=  EIGHTH_NUMBER_2; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when EIGHTH_NUMBER_2 =>
      next_state <= EIGHTH_NUMBER_2;
		if (KEY(7) = '1') then
         next_state <=  NINETH_NUMBER_2; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when NINETH_NUMBER_2 =>
      next_state <= NINETH_NUMBER_2;
		if (KEY(6) = '1') then
         next_state <=  TENTH_NUMBER_2; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when TENTH_NUMBER_2 =>
      next_state <= TENTH_NUMBER_2;
		if (KEY(8) = '1') then
         next_state <=  FINAL; 		
      elsif (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	 when FINAL =>
      next_state <= FINAL;		
      if (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_SUCCESS;
		elsif (KEY(14 downto 0) /= "000000000000000") then
			next_state <= BAD_NUMBER;
      end if;
	-- - - - - - - - - - - - - - - - - - - - - - -
	 when BAD_NUMBER =>
      next_state <= BAD_NUMBER;
      if (KEY(15) = '1') then
         next_state <= PRINT_MESSAGE_ERROR;
      end if;	
   -- - - - - - - - - - - - - - - - - - - - - - -
   when PRINT_MESSAGE_ERROR =>
      next_state <= PRINT_MESSAGE_ERROR;
      if (CNT_OF = '1') then
         next_state <= FINISH;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
	when PRINT_MESSAGE_SUCCESS =>
      next_state <= PRINT_MESSAGE_SUCCESS;
      if (CNT_OF = '1') then
         next_state <= FINISH;
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
   when FINISH =>
      next_state <= FINISH;
      if (KEY(15) = '1') then
         next_state <= FIRST_NUMBER; 
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
   when others =>
      next_state <= FIRST_NUMBER;
   end case;
end process next_state_logic;

-- -------------------------------------------------------
output_logic : process(present_state, KEY)
begin
   FSM_CNT_CE     <= '0';
   FSM_MX_MEM     <= '0';
   FSM_MX_LCD     <= '0';
   FSM_LCD_WR     <= '0';
   FSM_LCD_CLR    <= '0';

   case (present_state) is
   -- - - - - - - - - - - - - - - - - - - - - - -
   --when FIRST_NUMBER | BAD_NUMBER | SECOND_NUMBER | THIRD_NUMBER | FOURTH_NUMBER | FIFTH_NUMBER | SIXTH_NUMBER_1 | SIXTH_NUMBER_2 | SEVENTH_NUMBER_1 | SEVENTH_NUMBER_2 | EIGHTH_NUMBER_1 | EIGHTH_NUMBER_2 | NINETH_NUMBER_1 | NINETH_NUMBER_2 | TENTH_NUMBER_1 | TENTH_NUMBER_2 =>
   -- - - - - - - - - - - - - - - - - - - - - - -
   when PRINT_MESSAGE_ERROR =>
      FSM_CNT_CE     <= '1';
      FSM_MX_LCD     <= '1';
      FSM_LCD_WR     <= '1';
   -- - - - - - - - - - - - - - - - - - - - - - -
	when PRINT_MESSAGE_SUCCESS =>
      FSM_CNT_CE     <= '1';
      FSM_MX_LCD     <= '1';
      FSM_LCD_WR     <= '1';
		FSM_MX_MEM     <= '1';
   -- - - - - - - - - - - - - - - - - - - - - - -
   when FINISH =>
      if (KEY(15) = '1') then
         FSM_LCD_CLR    <= '1';
      end if;
   -- - - - - - - - - - - - - - - - - - - - - - -
   when others =>
	   if (KEY(14 downto 0) /= "000000000000000") then
         FSM_LCD_WR     <= '1';
      end if;
      if (KEY(15) = '1') then
         FSM_LCD_CLR    <= '1';
      end if;
   end case;
end process output_logic;

end architecture behavioral;

