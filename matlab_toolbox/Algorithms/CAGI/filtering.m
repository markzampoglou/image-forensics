function [meansmallAreas, meanbigAreas, meanImg]=filtering(smap)


 blocks=size(smap,1);
 step=sqrt(blocks);
 smallAreas=zeros(6,6);
 for a=1:6
     Start=a*(blocks/6)-(blocks/6)+1;
     End=a*(blocks/6);
    for x= Start:step:End
           
            for y=1:step/6
                z=x+y-1;
               if a<4
                smallAreas(1,(a*2)-1)=smallAreas(1,(a*2)-1)+smap(z,2);
                smallAreas(1,a*2)=smallAreas(1,a*2)+smap(z+step/6,2);

                smallAreas(2,(a*2)-1)=smallAreas(2,(a*2)-1)+smap(z+2*(step/6),2);
                smallAreas(2,a*2)=smallAreas(2,a*2)+smap(z+3*(step/6),2);
                
                smallAreas(3,(a*2)-1)=smallAreas(3,(a*2)-1)+smap(z+4*(step/6),2);
                smallAreas(3,a*2)=smallAreas(3,a*2)+smap(z+5*(step/6),2);
               else 
                smallAreas(4,((a-3)*2)-1)=smallAreas(4,((a-3)*2)-1)+smap(z,2);
                smallAreas(4,(a-3)*2)=smallAreas(4,(a-3)*2)+smap(z+step/6,2);
                
                smallAreas(5,((a-3)*2)-1)=smallAreas(5,((a-3)*2)-1)+smap(z+2*(step/6),2);
                smallAreas(5,(a-3)*2)=smallAreas(5,(a-3)*2)+smap(z+3*(step/6),2);
                
                smallAreas(6,((a-3)*2)-1)=smallAreas(6,((a-3)*2)-1)+smap(z+4*(step/6),2);
                smallAreas(6,(a-3)*2)=smallAreas(6,(a-3)*2)+smap(z+5*(step/6),2);
               end

            end
    end
 end
 meansmallAreas=smallAreas/100;
 meanbigAreas=zeros(1,6);
 for x=1:6
     meanbigAreas(1,x)=mean(meansmallAreas(x,:));
 end
 meanImg=mean(meanbigAreas);


end