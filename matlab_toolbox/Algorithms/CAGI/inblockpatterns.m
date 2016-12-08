
function [K,Correct, BlockScoreALL]=inblockpatterns(imageGS,bins,p,q,blk_idx, blk_idy)

image=imageGS; 
Zmat=zeros(floor(blk_idx*blk_idy),2);
a=0;

for i=1:blk_idx
     Ax=(i-1)*8+p;
      Ex=Ax+4;
  for j=1:blk_idy

     
      Ay=(j-1)*8+q;
      A=image(Ax,Ay);
      B=image(Ax, Ay+1);
      C=image(Ax+1, Ay);
      D=image(Ax+1, Ay+1);
    
      
     
      Ey=Ay+4;
      E=image(Ex,Ey);
      F=image(Ex, Ey+1);
      G=image(Ex+1, Ey);
      H=image(Ex+1, Ey+1);
      
   
      a=a+1;

      Zmat(a,1)=abs(A-B-C+D);
      Zmat(a,2)=abs(E-F-G+H);
        

      BlockScoreALL(i,j)= Zmat(a,2)- Zmat(a,1);

          if BlockScoreALL(i,j)==0  || BlockScoreALL(i,j)<0 
         
             BlockScoreALL(i,j)=0;

           end
      
  end;
end;

norm=a;

%//////// Normalized histograms////////
% Block inner
Hz=hist(Zmat(:,1),bins);
Hzn=Hz/norm;
%Block intersections
Hz2=hist(Zmat(:,2),bins);
Hz2n=Hz2/norm;
%////////////Energy//////////////////////
[x2,y2]=size(Hzn);
K=0;
K_temp=0;

for i=1:y2
  
   K_temp=Hzn(i)-Hz2n(i);
   K=K+abs(K_temp);

end;

A=sum(Hzn(1:2));

E=sum(Hz2n(1:2));

if A>E
    Correct=true;
else
    Correct=false;
end


end