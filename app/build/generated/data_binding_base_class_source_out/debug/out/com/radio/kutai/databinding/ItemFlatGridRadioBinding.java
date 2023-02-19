// Generated by view binder compiler. Do not edit!
package com.radio.kutai.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.balysv.materialripple.MaterialRippleLayout;
import com.like.LikeButton;
import com.radio.kutai.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemFlatGridRadioBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final LikeButton btnFavourite;

  @NonNull
  public final ImageView imgRadio;

  @NonNull
  public final MaterialRippleLayout layoutRipple;

  @NonNull
  public final LinearLayout layoutRoot;

  @NonNull
  public final TextView tvDes;

  @NonNull
  public final TextView tvName;

  private ItemFlatGridRadioBinding(@NonNull RelativeLayout rootView,
      @NonNull LikeButton btnFavourite, @NonNull ImageView imgRadio,
      @NonNull MaterialRippleLayout layoutRipple, @NonNull LinearLayout layoutRoot,
      @NonNull TextView tvDes, @NonNull TextView tvName) {
    this.rootView = rootView;
    this.btnFavourite = btnFavourite;
    this.imgRadio = imgRadio;
    this.layoutRipple = layoutRipple;
    this.layoutRoot = layoutRoot;
    this.tvDes = tvDes;
    this.tvName = tvName;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemFlatGridRadioBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemFlatGridRadioBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_flat_grid_radio, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemFlatGridRadioBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btn_favourite;
      LikeButton btnFavourite = rootView.findViewById(id);
      if (btnFavourite == null) {
        break missingId;
      }

      id = R.id.img_radio;
      ImageView imgRadio = rootView.findViewById(id);
      if (imgRadio == null) {
        break missingId;
      }

      id = R.id.layout_ripple;
      MaterialRippleLayout layoutRipple = rootView.findViewById(id);
      if (layoutRipple == null) {
        break missingId;
      }

      id = R.id.layout_root;
      LinearLayout layoutRoot = rootView.findViewById(id);
      if (layoutRoot == null) {
        break missingId;
      }

      id = R.id.tv_des;
      TextView tvDes = rootView.findViewById(id);
      if (tvDes == null) {
        break missingId;
      }

      id = R.id.tv_name;
      TextView tvName = rootView.findViewById(id);
      if (tvName == null) {
        break missingId;
      }

      return new ItemFlatGridRadioBinding((RelativeLayout) rootView, btnFavourite, imgRadio,
          layoutRipple, layoutRoot, tvDes, tvName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
