function [smap]=filteringMethod (smap, ThressSmall, ThressBig, ThressImg)

 blocks=size(smap,1);
 step=sqrt(blocks);
 
for x=1:6
    if ThressBig(x)<ThressImg && ThressImg<10
        ThressBig(x)=ThressImg;
    elseif ThressBig(x)>ThressImg && ThressImg<5
        ThressBig(x)=5;
    end
    for y=1:6
       if ThressSmall(x,y)<ThressBig(x)
           if ThressBig(x)<5
               ThressSmall(x,y)=ThressBig(x)+1;
           else
               ThressSmall(x,y)=ThressBig(x);
           end
       end
    end
end

Thresses=ThressSmall;

for a=1:6
     Start=a*(blocks/6)-(blocks/6)+1;
     End=a*(blocks/6);
    for x=Start:step:End
        for y=1:step/6
            z=x+y-1;
           
                 if a<4              
                   if smap(z,2)< Thresses(1,(a*2)-1);
                       smap(z,1)=59;
                   end
                   if smap(z+step/6,2)<Thresses(1,a*2);
                       smap(z+step/6,1)=59;
                   end

                   if smap(z+2*(step/6),2)<Thresses(2,(a*2)-1);
                       smap(z+2*(step/6),1)=59;
                   end
                   if smap(z+3*(step/6),2)<Thresses(2,a*2);
                        smap(z+3*(step/6),1)=59;
                   end

                   if smap(z+4*(step/6),2)<Thresses(3,(a*2)-1);
                        smap(z+4*(step/6),1)=59;
                   end
                   if smap(z+5*(step/6),2)<Thresses(3,a*2);
                        smap(z+5*(step/6),1)=59;
                   end
                else
                   if smap(z,2)< Thresses(4,((a-3)*2)-1);
                       smap(z,1)=59;
                   end
                   if smap(z+step/6,2)<Thresses(4,(a-3)*2);
                       smap(z+step/6,1)=59;
                   end

                   if smap(z+2*(step/6),2)<Thresses(5,((a-3)*2)-1);
                       smap(z+2*(step/6),1)=59;
                   end
                   if smap(z+3*(step/6),2)<Thresses(5,(a-3)*2);
                        smap(z+3*(step/6),1)=59;
                   end

                   if smap(z+4*(step/6),2)<Thresses(6,((a-3)*2)-1);
                        smap(z+4*(step/6),1)=59;
                   end
                   if smap(z+5*(step/6),2)<Thresses(6,(a-3)*2);
                        smap(z+5*(step/6),1)=59;
                   end
                end
                  
        end
    end
end
            
    



end