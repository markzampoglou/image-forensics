%   The noise level estimation code is shared by the following authors:
%   [17]Pyatykh S, Hesser J, Zheng L. Image Noise Level Estimation by Principal
%   Component Analysis (J). IEEE Transactions on Image Processing, 2013,
%   22(2):687–699.
function [label, variance] = PCANoiseLevelEstimator( image, Bsize )

%==========================================================================
% Parameters
%==========================================================================
	UpperBoundLevel             = 0.0005;   % 此处有讲究！！
	UpperBoundFactor            = 3.1;
	M1                          = Bsize;
	M2                          = Bsize;
	M                           = M1 * M2;
	EigenValueCount             = 7;
	EigenValueDiffThreshold     = 49.0;
	LevelStep                   = 0.05;
	MinLevel                    = 0.06;
	MaxClippedPixelCount        = round(0.1*M);        % = 0.1 * M
%==========================================================================
    label = 0;
    block_info = ComputeBlockInfo( image );
  if length(block_info)==0       %%%%% HZ
        label = 1;
        variance = var(image(:));
  else
    block_info = sortrows( block_info, [1] );    
    [sum1, sum2, subset_size] = ComputeStatistics( image, block_info );
    if subset_size(end) == 0      %%%% HZ
        label = 1;
        variance = var(image(:));
    else
    
    upper_bound = ComputeUpperBound( block_info );
    prev_variance = 0;
    variance = upper_bound;
    
    for iter = 1 : 10        
        if( abs(prev_variance - variance) < 1e-5 )   % HZ
            break
        end       
        prev_variance = variance;
        variance = GetNextEstimate( sum1, sum2, subset_size, variance, upper_bound );
    end
    if variance < 0                         %%%%% HZ
        label = 1;
        variance = var(image(:));
    end
    end
   end
%==========================================================================

    function y = Clamp( x, a, b )      
        y = x;       
        if x < a
            y = a;
        end        
        if x > b
            y = b;
        end
    end
%==========================================================================
    
    function block_info = ComputeBlockInfo( image )        
        block_info = zeros( size(image,1)*size(image,2), 3 );        
        block_count = 0;
        
        for y = 1 : size(image,1) - M2
            for x = 1 : size(image,2) - M1
                
                sum1 = 0.0;
                sum2 = 0.0;
                clipped_pixel_count = 0;
                
                for by = y : y + M2 - 1
                    for bx = x : x + M1 - 1
                        
                        val = image(by,bx);
                        sum1 = sum1 + val;
                        sum2 = sum2 + val*val;
                        
                        if val == 0 || val == 255
                            clipped_pixel_count = clipped_pixel_count + 1;
                        end
                    end
                end
                
                if clipped_pixel_count <= MaxClippedPixelCount                    
                    block_count = block_count + 1;
                    
                    block_info(block_count,1) = (sum2 - sum1*sum1/M) / M;
                    block_info(block_count,2) = x;
                    block_info(block_count,3) = y;
                end
            end
        end        
        block_info(block_count+1:size(image,1)*size(image,2),:) = [];
    end
%==========================================================================

    function [sum1, sum2, subset_size] = ComputeStatistics( image, block_info )
        
        sum1 = [];
        sum2 = [];
        subset_size = [];        
        subset_count = 0;
        
        for p = 1 : -LevelStep : MinLevel
            
            q = 0;
            if p - LevelStep > MinLevel
                q = p - LevelStep;
            end
            
            max_index = size(block_info,1) - 1;
            beg_index = Clamp( round(q*max_index) + 1, 1, size(block_info,1) );
            end_index = Clamp( round(p*max_index) + 1, 1, size(block_info,1) );
            
            curr_sum1 = zeros( M, 1 );
            curr_sum2 = zeros( M, M );
            
            for k = beg_index : end_index - 1
                curr_x = block_info(k,2);
                curr_y = block_info(k,3);
                
                block = reshape( image(curr_y : curr_y+M2-1, curr_x : curr_x+M1-1), M, 1 );
                curr_sum1 = curr_sum1 + block;
                curr_sum2 = curr_sum2 + block * block';
            end
            subset_count = subset_count + 1;
            
            sum1(:,:,subset_count) = curr_sum1;
            sum2(:,:,subset_count) = curr_sum2;
            subset_size(subset_count) = end_index - beg_index;
        end
        
        for i = length(subset_size) : -1 : 2
            
            sum1(:,:,i-1) = sum1(:,:,i-1) + sum1(:,:,i);
            sum2(:,:,i-1) = sum2(:,:,i-1) + sum2(:,:,i);
            subset_size(i-1) = subset_size(i-1) + subset_size(i);
        end
    end
%==========================================================================

    function upper_bound = ComputeUpperBound( block_info )        
        max_index = size(block_info, 1) - 1;
        %%%%%%
        nozeroindex = min(max(find (block_info(:,1)== 0))+1,size(block_info, 1));
        %%%%%%%%%%
        index = Clamp( round(UpperBoundLevel*max_index) + 1, nozeroindex, size(block_info, 1) );
        upper_bound = UpperBoundFactor * block_info(index,1);
    end
%==========================================================================

    function eigen_value = ApplyPCA( sum1, sum2, subset_size )        
        mean = sum1 ./ subset_size;
        cov_matrix = sum2 ./ subset_size - mean * mean';
        eigen_value = sort( eig(cov_matrix) );
    end
%==========================================================================

    function variance = GetNextEstimate( sum1, sum2, subset_size, prev_estimate, upper_bound )
        variance = 0;       
        for i =  1 : length(subset_size)    
 % [17] II.F, 'discards blocks with the largest variance by reducing p to 1-p, 1-2p, and so on'
            eigen_value = ApplyPCA( sum1(:,:,i), sum2(:,:,i), subset_size(i) );
            variance = eigen_value(1);
            if variance < 1e-5          %% HZ
                break;
            end
            diff            = eigen_value(EigenValueCount) - eigen_value(1);
            diff_threshold  = EigenValueDiffThreshold * prev_estimate / subset_size(i)^0.5;

            if( diff < diff_threshold && variance < upper_bound )
                break;
            end
        end
    end
%==========================================================================
variance = sqrt(variance);
end
