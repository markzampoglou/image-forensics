function [edgeImg2, edgeImg,edgeImg3]=PaintimgEdges(smap, MMasks, scale)

 if scale==1
     blocks=3600;
     stepX=60;  
 end
 
 edgeImg=zeros(600,600);
 countx=0;
 for a=1:stepX
        for b=1:stepX
            countx=countx+1;
            i=0;
            for x=(a-1)*10+1:(a-1)*10+10
                i=i+1;
                j=0;
                for y=(b-1)*10+1:(b-1)*10+10
                    j=j+1;
               
                
                    edgeImg(x,y)=MMasks(i,j,smap(countx,1));
                    if smap(countx,1)==59;
                    edgeImg3(x,y)=0;
                    else
                         edgeImg3(x,y)=1;
                    end
                   edgeImg2(x,y)=smap(countx,2);
                    
                end
            end
        end
 end
 
    end