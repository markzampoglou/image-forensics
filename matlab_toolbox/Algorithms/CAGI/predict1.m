function [PossiblePoints]=predict1(Kscores,Kpredict,Kpre)
for i=1:4
    for j=1:4
             A(i,j)=Kscores(i,j,1) + Kscores(i+4,j+4, 1) -Kscores(i+4,j,1)-Kscores(i,j+4,1);
    end
end

    r1=[1;2;3;4;1;2;3;4;1;2;3;4;1;2;3;4];
    c1=[1;1;1;1;2;2;2;2;3;3;3;3;4;4;4;4];


 PossiblePoints=zeros(numel(r1),8);
 
 for i=1: numel(r1);
     r=r1(i);
     c=c1(i);
     
             if A(r,c)>0 
                         if Kpredict(r,c)==1
                             A_point(1)=r;
                             A_point(2)=c;
                             E_point(1)=r+4;
                             E_point(2)=c+4;
                         else 
                             E_point(1)=r;
                             E_point(2)=c;
                             A_point(1)=r+4;
                             A_point(2)=c+4;
                         end
             else
                         if Kpredict(r,c+4)==1
                             A_point(1)=r;
                             A_point(2)=c+4;
                             E_point(1)=r+4;
                             E_point(2)=c;   
                         else
                             E_point(1)=r;
                             E_point(2)=c+4;
                             A_point(1)=r+4;
                             A_point(2)=c;  
                         end
             end
             PossiblePoints(i,1)= A_point(1);
             PossiblePoints(i,2)= A_point(2);
             PossiblePoints(i,3)= E_point(1);
             PossiblePoints(i,4)= E_point(2);

             PossiblePoints(i,5)= Kscores(r,c)/2;
             PossiblePoints(i,6)=0;  
             
 end

         
 
 for i=1:numel(r1)
 PossiblePoints(i,7)=Kpre(PossiblePoints(i,1),PossiblePoints(i,2)) ...
                                      -Kpre(PossiblePoints(i,3),PossiblePoints(i,4));
     PossiblePoints(i,8)=(PossiblePoints(i,7)  + PossiblePoints(i,5))/2;                    
 end;
  