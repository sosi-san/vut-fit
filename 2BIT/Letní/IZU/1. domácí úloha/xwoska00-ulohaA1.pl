ulohaA1(L,A,LOUT) :-
    isAscending(L),
    isOkay(LOUT,A),
    isInL(LOUT,L),
    checkSubList(LOUT).  

isAscending([_]).
isAscending([X,H|T]) :- 
    X=<H, isAscending([H|T]).

first([A|_],A).

last([X],END):-
	END = X.

last([_|T],END):-
	last(T,END).

isOkay([],_).
isOkay([L|T],A):-
    first(L,X),
    last(L,Y),
    Y-X=<A,
    isOkay(T,A).

isMember([],_).
isMember([H|T],L):-
    member(H,L),
    isMember(T,L).

isInL([],_).
isInL([H|T],L):-
    isMember(H,L),
    isInL(T,L).

loopIt([],[]).
loopIt(_,[]).
loopIt([],_).
loopIt([H1|T1],[H2|T2]):-
    not(H1=H2),
    loopIt(T1,T2).

isSublist(_,[]).
isSublist(H,[H2|T]):-
    loopIt(H,H2),
    isSublist(H,T). 

checkSubList([_|[]]):- true.
checkSubList([H|T]):-
    isSublist(H,T),
    checkSubList(T).

 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    