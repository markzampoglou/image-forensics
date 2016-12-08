function [MeanInSpace,PossiblePoints,diff_Mean_Best_scaled,diff_Mean_Best_scaledInv]=scores_pick_variables(BlockScoreALL,sgrid,blk_idx,blk_idy,PossiblePoints,kx,ky)

 
BlockScore=zeros(floor(blk_idx),floor(blk_idy),16);    
  for i=1:16

        p= PossiblePoints(i,1);
        q=PossiblePoints(i,2);

        BlockScore(:,:,i)=BlockScoreALL(:,:,p,q)/255;
   end

   for r=1:16
        for i=1:kx
            for j=1:ky
                b=i*sgrid;
                a=b-(sgrid-1);
                d=j*sgrid;
                c=d-(sgrid-1);
                MeanInSpace(i,j,r)=mean(reshape(BlockScore(a:b,c:d,r),[1,sgrid^2]));
             end
        end
   end
                

                
               
     for i=1:kx
            for j=1:ky
                for r=1:16               
                     odd(r)=MeanInSpace(i,j,r);
                end
                     MeanOfAllGrids(i,j)=mean(odd(:));
            end
     end

      
   BestGrid(:,:)=MeanInSpace(:,:,16);
   diff_Mean_Best=MeanOfAllGrids(:,:) - BestGrid(:,:); 
   diff_Mean_Best_scaled=mat2gray(diff_Mean_Best); 
   
                %extra for inverse 
            %start-------------------------------------------------------

                 bg=0;
            for f=1:16
                if PossiblePoints(f,1)==4 && PossiblePoints(f,2)==4
                    bg=f;
                  
                  end
            end
            
            for f=1:16
              
            if bg==0
                    bg1= find(PossiblePoints(:,5)==max(PossiblePoints(:,5)));
                    bg=max(bg1);
                   
            end
            end
            
            %Extra for inverse
            %end-------------------------------------------------------

   BestGridInv(:,:)=MeanInSpace(:,:,bg);
   diff_Mean_BestInv=MeanOfAllGrids(:,:) - BestGridInv(:,:); 
   diff_Mean_Best_scaledInv=mat2gray(diff_Mean_BestInv); 

