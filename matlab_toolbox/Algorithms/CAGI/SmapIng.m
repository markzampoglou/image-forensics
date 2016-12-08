function [smap]=SmapIng(ImgTiles, MaskTiles, WhiteMaskPoints)
 blocks=size(ImgTiles,3);
 smap=zeros(blocks,2);

winMask=59;
    for a=1:blocks
         maxR=0;
         for k=1:58
             TempW=0;
             TempB=0;
            for x=1:10
                for y=1:10
                   if MaskTiles(x,y,k)>0
                       TempW=TempW+ImgTiles(x,y,a);
                   else 
                       TempB=TempB+ImgTiles(x,y,a);
                   end;
                end
            end

                whiteScore=TempW/WhiteMaskPoints(k,1);
                blackScore=TempB/(100-WhiteMaskPoints(k,1));
                ctR=abs(whiteScore-blackScore);
                w=((ctR*100)/255);
                if w>maxR
                    maxR=w;
                    winMask=k;
                end

         end %k
         smap(a,1)=winMask;
         smap(a,2)=maxR;
    end %blocks

end