 function [tile]=ImageTiling(Img)

Img=imresize(Img, [600 600],'nearest');
[d1,d2]=size(Img);
if d2>601      
  R1=rgb2gray(Img);
  R=(im2double(R1)*255); 
else
    R=(im2double(Img)*255);
end;


    blocks=3600;
    stepX=60;
    stepY=60;
    ImgR=R;

    countx=0;
    ptr=1;
    tile=zeros(10,10,blocks);
    for a=1:stepX
        for b=1:stepY
            countx=countx+1;
            i=0;
            for x=(a-1)*10+1:(a-1)*10+10
                i=i+1;
                j=0;
                for y=(b-1)*10+1:(b-1)*10+10
                    j=j+1;
                    temp(ptr,1) = ImgR(x,y);
                    ptr=ptr+1;
                    tile(i,j,countx)=ImgR(x,y);
                    
                end
            end
        end
        
    end

    
 end