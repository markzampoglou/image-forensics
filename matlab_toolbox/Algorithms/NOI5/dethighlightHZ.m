function highlighted= dethighlightHZ(im,blocksize,detections)
%%%%% 
% detections:predictlabel:0 or 1
% The purpose of this function is to highlight blocks which have been
% identified as tampered.
% written by Matthew C Stamm 4/09
%%%%%%%%%%%%%%%%%%%%%%
im = im';
rval = 255;bval = 0;gval = 0;

[rows cols colors]= size(im);
rowblocks = floor(rows/blocksize);   
% caculate the number of blocks contained in the colnum
colblocks = floor(cols/blocksize);     
% caculate the number of blocks contained in the rownum %cols/blocksize;
if colors == 1
    newim(:,:,1)= im;
    newim(:,:,2)= im;
    newim(:,:,3)= im;
    im = newim;
end
% pick red color layer for highlighting
highlighted= im;

for rowblock= 1:rowblocks
    for colblock= 1:colblocks        
        if detections(rowblock,colblock) == 2       
    % lable 2 in Kmeans denotes tampered area.
            rowst= (rowblock-1) * blocksize+ 1;
            rowfin= rowblock * blocksize;
            colst= (colblock-1) * blocksize + 1;
            colfin= colblock * blocksize; 
            % red
            highlighted(rowst:rowst+2,colst:colfin,1)= rval;
            highlighted(rowfin-2:rowfin,colst:colfin,1)= rval;
            highlighted(rowst:rowfin,colst:colst+2,1)= rval;
            highlighted(rowst:rowfin,colfin-2:colfin,1)= rval;         
            
            % green
            highlighted(rowst:rowst+2,colst:colfin,2)= gval;
            highlighted(rowfin-2:rowfin,colst:colfin,2)= gval;
            highlighted(rowst:rowfin,colst:colst+2,2)= gval;
            highlighted(rowst:rowfin,colfin-2:colfin,2)= gval;
            
            % blue
            highlighted(rowst:rowst+2,colst:colfin,3)= bval;
            highlighted(rowfin-2:rowfin,colst:colfin,3)= bval;
            highlighted(rowst:rowfin,colst:colst+2,3)= bval;
            highlighted(rowst:rowfin,colfin-2:colfin,3)= bval;         
            
            if rowst-1 > 0
                highlighted(rowst-3:rowst-1,colst:colfin,1)= rval;
                highlighted(rowst-3:rowst-1,colst:colfin,2)= gval;
                highlighted(rowst-3:rowst-1,colst:colfin,3)= bval;
               
                if colst-1 > 0
                    highlighted(rowst-3:rowst-1,colst-3:colst-1,1)= rval;
                    highlighted(rowst-3:rowst-1,colst-3:colst-1,2)= gval;
                    highlighted(rowst-3:rowst-1,colst-3:colst-1,3)= bval;  
                end
                if colfin+1 < cols
                    highlighted(rowst-3:rowst-1,colfin+1:colfin+3,1)= rval;
                    highlighted(rowst-3:rowst-1,colfin+1:colfin+3,2)= gval;
                    highlighted(rowst-3:rowst-1,colfin+1:colfin+3,3)= bval;
                end
            end
            
            if rowfin+1 < rows
                highlighted(rowfin+1:rowfin+3,colst:colfin,1)= rval;
                highlighted(rowfin+1:rowfin+3,colst:colfin,2)= gval;
                highlighted(rowfin+1:rowfin+3,colst:colfin,3)= bval;
                
                if colst-1 > 0
                    highlighted(rowfin+1:rowfin+3,colst-3:colst-1,1)= rval;
                    highlighted(rowfin+1:rowfin+3,colst-3:colst-1,2)= gval;
                    highlighted(rowfin+1:rowfin+3,colst-3:colst-1,3)= bval;
                end
                if colfin+1 < cols
                    highlighted(rowfin+1:rowfin+3,colfin+1:colfin+3,1)= rval;
                    highlighted(rowfin+1:rowfin+3,colfin+1:colfin+3,2)= gval;
                    highlighted(rowfin+1:rowfin+3,colfin+1:colfin+3,3)= bval;
                end
            end
            
            if colst-1 > 0
                highlighted(rowst:rowfin,colst-3:colst-1,1)= rval;
                highlighted(rowst:rowfin,colst-3:colst-1,2)= gval;
                highlighted(rowst:rowfin,colst-3:colst-1,3)= bval;
            end
            
            if colfin+1 < cols
                highlighted(rowst:rowfin,colfin+1:colfin+3,1)= rval;
                highlighted(rowst:rowfin,colfin+1:colfin+3,2)= gval;
                highlighted(rowst:rowfin,colfin+1:colfin+3,3)= bval;
            end            
        end        
    end
end
highlighted= uint8(highlighted);

highlighted_temp(:,:,1)= highlighted(:,:,1)';
highlighted_temp(:,:,2)= highlighted(:,:,2)';
highlighted_temp(:,:,3)= highlighted(:,:,3)';
highlighted= highlighted_temp;

% figure,imshow(highlighted);
figure,imshow(highlighted,'Border','tight');