function [Kpredict,Kpre]=predict0(Kscores)

Kpredict=zeros(9,9);
Kpredict(1:8,1:8)=Kscores(:,:,2);
for i=1:8
    Kpredict(9,i)=sum(Kpredict(:,i));
    
     Kpredict(i,9)=sum(Kpredict(i,:));
 
end
Kpre=zeros(8,8);
for i=1:8
    for j=1:8
        Kpre(i,j)=(Kpredict(i,9)+Kpredict(9,j))/16;
    end
end

