function [OutputMap, Feature_Vector, coeffArray] = Extract_Features( im )
% Mark Zampoglou
% Implementation of  "Fast, automatic and fine-grained tampered JPEG image
% detection via DCT coefficient analysis" by Lin et al


if isstruct(im)
     [OutputMap, Feature_Vector, coeffArray] = Extract_Features_JPEG( im );
else
    [OutputMap, Feature_Vector, coeffArray] = Extract_Features_NonJPEG( im );
end


end

