package co.tamara.example.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.tamara.example.R
import co.tamara.example.model.EAmount
import co.tamara.example.model.EItem
import co.tamara.example.viewmodel.ViewModelFactory
import co.tamara.sdk.TamaraPayment
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_shop.*
import kotlinx.android.synthetic.main.item_toy.*
import kotlinx.android.synthetic.main.item_toy.view.*

class ShopFragment : Fragment() {

    private lateinit var orderVieModel: OrderViewModel
    private var items: List<EItem>? = null
    private lateinit var viewModel: ShopViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shop, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), ViewModelFactory()).get(ShopViewModel::class.java)
        orderVieModel = ViewModelProvider(requireActivity()).get(OrderViewModel::class.java)
        viewModel.items.observe(viewLifecycleOwner, Observer {
            items = it
            shopList.adapter?.notifyDataSetChanged()
        })
        shopList.adapter = ShopAdapter()
        checkoutBtn.setOnClickListener {
            items?.forEach {
                if (it.quantity > 0) {
                    TamaraPayment.addItem(
                        it.name,
                        it.referenceId,
                        it.sku,
                        it.unitPrice?.amount ?: 0.0,
                        it.taxAmount?.amount ?: 0.0,
                        it.discountAmount?.amount ?: 0.0,
                        it.quantity
                    )
                }
            }
            items?.let { items -> orderVieModel.updateItems(items) }
            findNavController(this).navigate(R.id.checkoutFragment)
        }
    }

    fun formatPrice(textView: TextView, originalPrice: String, discountedPrice: String) {
        if (originalPrice != discountedPrice) {
            val text = "$originalPrice $discountedPrice"
            textView.setText(text, TextView.BufferType.SPANNABLE)
            val spannable = textView.text as Spannable
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#9E9E9E")),
                0,
                originalPrice.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val span = StrikethroughSpan()
            spannable.setSpan(span, 0, originalPrice.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val bold = StyleSpan(Typeface.BOLD)
            spannable.setSpan(
                bold,
                originalPrice.length + 1,
                text.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            textView.setText(originalPrice, TextView.BufferType.SPANNABLE)
            val bold = StyleSpan(Typeface.BOLD)
            val spannable = textView.text as Spannable
            spannable.setSpan(
                bold,
                0,
                originalPrice.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    inner class ShopAdapter : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_toy, parent, false)
            view.addBtn.setOnClickListener {
                val position = it.tag as Int
                items!![position].quantity = (items!![position].quantity + 1).coerceAtMost(5)
                notifyDataSetChanged()
            }
            view.removeBtn.setOnClickListener {
                val position = it.tag as Int
                items!![position].quantity = (items!![position].quantity - 1).coerceAtLeast(0)
                notifyDataSetChanged()
            }
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return items?.size ?: 0
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items!![position]
            holder.itemNameTxt.text = item.name
            holder.skuTxt.text = item.sku
            val discounted = EAmount(
                (item.unitPrice!!.amount - (item.discountAmount?.amount
                    ?: 0.0)), item.unitPrice!!.currency
            )
            formatPrice(holder.priceTxt, item.unitPrice!!.getFormattedAmount(), discounted.getFormattedAmount())
            holder.taxTxt.text = getString(R.string.tax_with_amount, item.taxAmount?.getFormattedAmount() ?: "0")
            holder.quantityEdit.text = item.quantity.toString()
            holder.addBtn.tag = position
            holder.removeBtn.tag = position
        }

        inner class ViewHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView!!), LayoutContainer

    }

}
