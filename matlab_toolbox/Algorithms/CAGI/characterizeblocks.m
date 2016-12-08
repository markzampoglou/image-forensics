 function [E, EInv]=characterizeblocks(MeanContent2,MeanStrongEdge, V_im, blk_idx,blk_idy, MeanInSpace,diff_Mean_Best_scaled,diff_Mean_Best_scaledInv,sgrid,PossiblePoints,kx,ky)

uniform=zeros(floor(blk_idx/sgrid),floor(blk_idy/sgrid));

for a=1:kx
    for b=1:ky
        for pp=1:16

              if MeanInSpace(a,b,pp,1)<mean(mean(MeanInSpace(:,:,pp,1))) *0.2
                uniform(a,b)=uniform(a,b)+1;  
              end
        
        end
    end
end


st=std(reshape(uniform, [],1));

H = fspecial('average', [5 5]);
I=imfilter(uniform, H);



meanv=mean(mean(I));


    bg=0;
            for f=1:16
                if PossiblePoints(f,1)==4 && PossiblePoints(f,2)==4
                    bg=f;
                end
            end
                
            if bg==16
                    bestgrid=mat2gray(imfilter(MeanInSpace(:,:,16,1),H));
            elseif bg==0
                    bg1= find(PossiblePoints(:,5)==max(PossiblePoints(:,5)));
                    bg=max(bg1);
                    bestgrid=mat2gray(imfilter(MeanInSpace(:,:,bg,1),H));
             else 
                    bestgrid=mat2gray(imfilter(MeanInSpace(:,:,bg,1),H));
            end
            

%//////////block based homogenous
    if mean(PossiblePoints(:,5))>0.4  || bg~=16
        homB=0;
    else 
        homB=1;
    end
  
    if st/meanv>1.5
         I(I<(meanv+(st)))=0;
         I(I>=(meanv+(st)))=homB; 
    else
             I(I<(meanv+(st)/2))=0;
            I(I>=(meanv+(st)/2))=homB; 
    end
    


%/////////no content////////////////////////


contentsc=(MeanContent2);

x24=floor(blk_idx/3);
y24=floor(blk_idy/3);

hom=zeros(kx,ky);
held=mean(contentsc(:));
for i=1:kx
    for j=1:ky
        if contentsc(i,j)<=4 %very soft responces
              hom(i,j)=1;

        end
    end;
end

   c=sgrid-1;
for i=1:kx
  for j=1:ky
      a=1+(i-1)*sgrid;
      b=1+(j-1)*sgrid;
   
      MeanStrongEdge2(i,j)=mean(mean(MeanStrongEdge(a:a+c, b:b+c)));

  end
  
end

cc=8*sgrid-1;
for i=1:kx
  for j=1:ky
      a=1+(i-1)*8*sgrid;
      b=1+(j-1)*8*sgrid;
      V_im2(i,j)=mean(mean(V_im(a:a+cc, b:b+cc)));
  end
  
end
V_imOver=V_im2;
V_imUndr=V_im2;
V_imOver(V_imOver>=245)=300;
V_imOver(V_imOver~=300)=0;
V_imUndr(V_imUndr<15)=300;
V_imUndr(V_imUndr~=300)=0;

V_imOver=mat2gray(V_imOver);
V_imUndr=mat2gray(V_imUndr);
MeanStrongEdge2(MeanStrongEdge2<0.5)=0;
MeanStrongEdge2(MeanStrongEdge2>=0.5)=1;

%/////////////end overexposed/iunder and contours////////////////////

          
touse=kx*ky;
notuse=zeros(kx,ky);
for i=1:kx
  for j=1:ky
      
         if  hom(i,j)==1 
                 notuse(i,j)=3;
         end
        
        if  MeanStrongEdge2(i,j)==1 
                 notuse(i,j)=2;
        end
         
        if   V_imUndr(i,j)==1 || V_imOver(i,j)==1  
                notuse(i,j)=1;
        end
    
  end
end

for i=1:kx
  for j=1:ky   
      if notuse(i,j)==1
          I(i,j)=1;
      end
  end
end

notused=sum(notuse(:)~=0);
touse=kx*ky-notused;
        %//////////////excl NaN
if touse==0
   for i=1:kx
     for j=1:ky   
               if  hom(i,j)==1 && I(i,j)~=1
                 notuse(i,j)=0;
               end
     end
    end
end

diff_Mean_Best_scaled_temp=diff_Mean_Best_scaled;
diff_Mean_Best_scaled_tempInv=diff_Mean_Best_scaledInv;
for a=1:floor(blk_idx/sgrid)
    for b=1:floor(blk_idy/sgrid)
        if I(a,b)==1
        diff_Mean_Best_scaled_temp(a,b)=0;
        diff_Mean_Best_scaled_tempInv(a,b)=1;
        end

        if diff_Mean_Best_scaled_temp(a,b)<mean(mean(diff_Mean_Best_scaled)) && homB==1
            diff_Mean_Best_scaled_temp(a,b)=0;
        end
         if diff_Mean_Best_scaled_tempInv(a,b)<mean(mean(diff_Mean_Best_scaledInv)) && homB==1
            diff_Mean_Best_scaled_tempInv(a,b)=1;
        end
    end
end
         

 for x=1:a
     for y=1:b
         if x==1 || x==a || y==1 || y==b
             imageF(x,y)=diff_Mean_Best_scaled_temp(x,y)*(bestgrid(x,y));
         else
             imageF(x,y)=diff_Mean_Best_scaled_temp(x,y)*(1-bestgrid(x,y));
         end
          imageFInv(x,y)=diff_Mean_Best_scaled_tempInv(x,y)*(1-bestgrid(x,y));
     end
 end
         
           E_nofilt=imageF;
           E=imfilter(imageF, H);
          
            E_nofiltInv=imageFInv;
           EInv=imfilter(imageFInv, H);

% /////////////content based filtering//////////

unintresting=zeros(touse,1);
unintrestingInv=zeros(touse,1);
a=0;
for i=1:kx
  for j=1:ky
    if  notuse(i,j)==0;
        a=a+1;
        unintresting(a)=E(i,j); 
        unintrestingInv(a)=EInv(i,j); 
    end
  end
end
% meanuninteresting=mean(E(:));
MeanBlocksre=E_nofilt; 
MeanBlocksreInv=E_nofiltInv; 
meanuninteresting=mean(unintresting);
meanuninterestingInv=mean(unintrestingInv);

for i=1:kx
  for j=1:ky
      if I(i,j)==1 && notuse(i,j)==2   
          I(i,j)=0;
      end
      if notuse(i,j)==1 || MeanBlocksre(i,j)<meanuninteresting
      MeanBlocksre(i,j)=meanuninteresting;
      end
      if (I(i,j)==1  && MeanBlocksre(i,j)<meanuninteresting )||  (notuse(i,j)==3 && I(i,j)==1)   
      MeanBlocksre(i,j)=meanuninteresting;
      end
      
        if notuse(i,j)==1 || MeanBlocksreInv(i,j)>meanuninterestingInv
      MeanBlocksreInv(i,j)=meanuninterestingInv;
      end
      if (I(i,j)==1  && MeanBlocksreInv(i,j)>meanuninterestingInv )||  (notuse(i,j)==3 && I(i,j)==1)   
      MeanBlocksreInv(i,j)=meanuninterestingInv;
      end
       
  end
end

E=imfilter(MeanBlocksre, H,'symmetric');
EInv=imfilter(MeanBlocksreInv, H,'symmetric');
            