output "cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_id
}

output "cluster_endpoint" {
  description = "EKS API server endpoint"
  value       = module.eks.cluster_endpoint
}

output "cluster_ca" {
  description = "EKS cluster CA data (base64)"
  value       = module.eks.cluster_certificate_authority_data
}

output "node_groups" {
  description = "EKS managed node group details"
  value       = module.eks.eks_managed_node_groups
}